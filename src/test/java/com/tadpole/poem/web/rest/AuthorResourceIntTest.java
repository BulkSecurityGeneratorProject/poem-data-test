package com.tadpole.poem.web.rest;

import com.tadpole.poem.PoemdataApp;
import com.tadpole.poem.domain.Author;
import com.tadpole.poem.repository.AuthorRepository;
import com.tadpole.poem.service.AuthorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the AuthorResource REST controller.
 *
 * @see AuthorResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PoemdataApp.class)
@WebAppConfiguration
@IntegrationTest
public class AuthorResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";
    private static final String DEFAULT_BIRTH_YEAR = "AAAAA";
    private static final String UPDATED_BIRTH_YEAR = "BBBBB";
    private static final String DEFAULT_DIE_YEAR = "AAAAA";
    private static final String UPDATED_DIE_YEAR = "BBBBB";
    private static final String DEFAULT_ZI = "AAAAA";
    private static final String UPDATED_ZI = "BBBBB";
    private static final String DEFAULT_HAO = "AAAAA";
    private static final String UPDATED_HAO = "BBBBB";
    private static final String DEFAULT_AVATAR_FILE_NAME = "AAAAA";
    private static final String UPDATED_AVATAR_FILE_NAME = "BBBBB";

    @Inject
    private AuthorRepository authorRepository;

    @Inject
    private AuthorService authorService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restAuthorMockMvc;

    private Author author;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AuthorResource authorResource = new AuthorResource();
        ReflectionTestUtils.setField(authorResource, "authorService", authorService);
        this.restAuthorMockMvc = MockMvcBuilders.standaloneSetup(authorResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        author = new Author();
        author.setName(DEFAULT_NAME);
        author.setBirthYear(DEFAULT_BIRTH_YEAR);
        author.setDieYear(DEFAULT_DIE_YEAR);
        author.setZi(DEFAULT_ZI);
        author.setHao(DEFAULT_HAO);
        author.setAvatarFileName(DEFAULT_AVATAR_FILE_NAME);
    }

    @Test
    @Transactional
    public void createAuthor() throws Exception {
        int databaseSizeBeforeCreate = authorRepository.findAll().size();

        // Create the Author

        restAuthorMockMvc.perform(post("/api/authors")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(author)))
                .andExpect(status().isCreated());

        // Validate the Author in the database
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(databaseSizeBeforeCreate + 1);
        Author testAuthor = authors.get(authors.size() - 1);
        assertThat(testAuthor.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAuthor.getBirthYear()).isEqualTo(DEFAULT_BIRTH_YEAR);
        assertThat(testAuthor.getDieYear()).isEqualTo(DEFAULT_DIE_YEAR);
        assertThat(testAuthor.getZi()).isEqualTo(DEFAULT_ZI);
        assertThat(testAuthor.getHao()).isEqualTo(DEFAULT_HAO);
        assertThat(testAuthor.getAvatarFileName()).isEqualTo(DEFAULT_AVATAR_FILE_NAME);
    }

    @Test
    @Transactional
    public void getAllAuthors() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authors
        restAuthorMockMvc.perform(get("/api/authors?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(author.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].birthYear").value(hasItem(DEFAULT_BIRTH_YEAR.toString())))
                .andExpect(jsonPath("$.[*].dieYear").value(hasItem(DEFAULT_DIE_YEAR.toString())))
                .andExpect(jsonPath("$.[*].zi").value(hasItem(DEFAULT_ZI.toString())))
                .andExpect(jsonPath("$.[*].hao").value(hasItem(DEFAULT_HAO.toString())))
                .andExpect(jsonPath("$.[*].avatarFileName").value(hasItem(DEFAULT_AVATAR_FILE_NAME.toString())));
    }

    @Test
    @Transactional
    public void getAuthor() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get the author
        restAuthorMockMvc.perform(get("/api/authors/{id}", author.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(author.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.birthYear").value(DEFAULT_BIRTH_YEAR.toString()))
            .andExpect(jsonPath("$.dieYear").value(DEFAULT_DIE_YEAR.toString()))
            .andExpect(jsonPath("$.zi").value(DEFAULT_ZI.toString()))
            .andExpect(jsonPath("$.hao").value(DEFAULT_HAO.toString()))
            .andExpect(jsonPath("$.avatarFileName").value(DEFAULT_AVATAR_FILE_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingAuthor() throws Exception {
        // Get the author
        restAuthorMockMvc.perform(get("/api/authors/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAuthor() throws Exception {
        // Initialize the database
        authorService.save(author);

        int databaseSizeBeforeUpdate = authorRepository.findAll().size();

        // Update the author
        Author updatedAuthor = new Author();
        updatedAuthor.setId(author.getId());
        updatedAuthor.setName(UPDATED_NAME);
        updatedAuthor.setBirthYear(UPDATED_BIRTH_YEAR);
        updatedAuthor.setDieYear(UPDATED_DIE_YEAR);
        updatedAuthor.setZi(UPDATED_ZI);
        updatedAuthor.setHao(UPDATED_HAO);
        updatedAuthor.setAvatarFileName(UPDATED_AVATAR_FILE_NAME);

        restAuthorMockMvc.perform(put("/api/authors")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedAuthor)))
                .andExpect(status().isOk());

        // Validate the Author in the database
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(databaseSizeBeforeUpdate);
        Author testAuthor = authors.get(authors.size() - 1);
        assertThat(testAuthor.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAuthor.getBirthYear()).isEqualTo(UPDATED_BIRTH_YEAR);
        assertThat(testAuthor.getDieYear()).isEqualTo(UPDATED_DIE_YEAR);
        assertThat(testAuthor.getZi()).isEqualTo(UPDATED_ZI);
        assertThat(testAuthor.getHao()).isEqualTo(UPDATED_HAO);
        assertThat(testAuthor.getAvatarFileName()).isEqualTo(UPDATED_AVATAR_FILE_NAME);
    }

    @Test
    @Transactional
    public void deleteAuthor() throws Exception {
        // Initialize the database
        authorService.save(author);

        int databaseSizeBeforeDelete = authorRepository.findAll().size();

        // Get the author
        restAuthorMockMvc.perform(delete("/api/authors/{id}", author.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(databaseSizeBeforeDelete - 1);
    }
}
