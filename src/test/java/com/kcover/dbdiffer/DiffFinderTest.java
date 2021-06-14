package com.kcover.dbdiffer;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;

class DiffFinderTest {

    @Test
    void test() throws Exception {
        for (int i = 0; i < 10; i++) {
            System.out.println(UUID.randomUUID());
        }
    }

    private static JdbcTemplate oldDbTemplate;
    private static JdbcTemplate newDbTemplate;

    @BeforeAll
    static void beforeAll() throws IOException {
        oldDbTemplate = createJdbcTemplate();
        String oldSql = IOUtils.toString(getResourceAsStream("oldDbData.sql"), StandardCharsets.UTF_8);
        oldDbTemplate.execute(oldSql);
        newDbTemplate = createJdbcTemplate();
        String newSql = IOUtils.toString(getResourceAsStream("newDbData.sql"), StandardCharsets.UTF_8);
        newDbTemplate.execute(newSql);
    }

    @Test
    void testFindMissingEntries() throws Exception{
        File outputFile = Files.createTempFile(null, ".txt").toFile();
        int pageSize = 100;
        DiffFinder.writeMissingAccountsToFile(oldDbTemplate, newDbTemplate, pageSize, outputFile);

        String outputFileString;
        try(FileReader reader = new FileReader(outputFile)){
            outputFileString = IOUtils.toString(reader);
        }
        String expectedFileString = IOUtils.toString(getResourceAsStream("missingAccountsExpected.txt"), StandardCharsets.UTF_8);

        assertThat(outputFileString, Matchers.is(expectedFileString));
    }

    @Test
    void testFindCorruptedEntries() throws Exception {
        File outputFile = Files.createTempFile(null, ".txt").toFile();
        int pageSize = 100;
        DiffFinder.writeCorruptedAccountsToFile(oldDbTemplate, newDbTemplate, pageSize, outputFile);

        String outputFileString;
        try(FileReader reader = new FileReader(outputFile)){
            outputFileString = IOUtils.toString(reader);
        }

        String expectedFileString = IOUtils.toString(getResourceAsStream("corruptedAccountsExpected.txt"), StandardCharsets.UTF_8);

        assertThat(outputFileString, Matchers.is(expectedFileString));
    }

    private static InputStream getResourceAsStream(String resource){
        return DiffFinderTest.class.getClassLoader().getResourceAsStream(resource);
    }

    /*
    Creates a jdbc template along with an h2 db backed by a temp directory
     */
    private static JdbcTemplate createJdbcTemplate() throws IOException {
        Path tempDir = Files.createTempDirectory(null);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:" + tempDir);
        dataSource.setUsername("sa");
        dataSource.setPassword("password");
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return template;
    }

}