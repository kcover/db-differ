package com.kcover.dbdiffer;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
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

    //missing records are 0004ddc3-e99a-419e-b14b-e91a2138416e, 000846f0-1043-414e-8ad2-7ad4955c6833
    @Test
    void testFindMissingEntries() throws Exception{
        //setup DBs
        JdbcTemplate oldDbTemplate = createJdbcTemplate();
        JdbcTemplate newDbTemplate = createJdbcTemplate();
        String oldSql = IOUtils.toString(getResourceAsStream("oldDbData.sql"), StandardCharsets.UTF_8);
        oldDbTemplate.execute(oldSql);
        String newSql = IOUtils.toString(getResourceAsStream("newDbData.sql"), StandardCharsets.UTF_8);
        newDbTemplate.execute(newSql);

        File outputFile = Files.createTempFile(null, ".txt").toFile();
        int pageSize = 100;
        DiffFinder.writeMissingRecordsToFile(oldDbTemplate, newDbTemplate, pageSize, outputFile);

        String outputFileString;
        try(FileReader reader = new FileReader(outputFile)){
            outputFileString = IOUtils.toString(reader);
        }
        String expectedFileString = IOUtils.toString(getResourceAsStream("missingRecordsExpected.txt"), StandardCharsets.UTF_8);

        assertThat(outputFileString, Matchers.is(expectedFileString));
    }

    private InputStream getResourceAsStream(String resource){
        return this.getClass().getClassLoader().getResourceAsStream(resource);
    }

    private JdbcTemplate createJdbcTemplate() throws IOException {
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