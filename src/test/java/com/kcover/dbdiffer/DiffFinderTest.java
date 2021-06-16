package com.kcover.dbdiffer;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class DiffFinderTest {

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
  void testFindMissingEntries() throws Exception {
    File outputFile = Files.createTempFile(null, ".txt").toFile();
    // Use page size 1 to check for off by 1 errors
    int pageSize = 1;
    try (FileWriter fileWriter = new FileWriter(outputFile)) {
      DiffFinder.writeMissingAccountsToFile(oldDbTemplate, newDbTemplate, pageSize, fileWriter);
    }

    String outputFileString;
    try (FileReader reader = new FileReader(outputFile)) {
      outputFileString = IOUtils.toString(reader);
    }
    String expectedFileString =
        IOUtils.toString(
            getResourceAsStream("missingAccountsExpected.txt"), StandardCharsets.UTF_8);

    assertThat(outputFileString, Matchers.is(expectedFileString));
  }

  @Test
  void testFindCorruptedEntries() throws Exception {
    File outputFile = Files.createTempFile(null, ".txt").toFile();
    int pageSize = 1;
    try (FileWriter fileWriter = new FileWriter(outputFile)) {
      DiffFinder.writeCorruptedAccountsToFile(oldDbTemplate, newDbTemplate, pageSize, fileWriter);
    }

    String outputFileString;
    try (FileReader reader = new FileReader(outputFile)) {
      outputFileString = IOUtils.toString(reader);
    }

    String expectedFileString =
        IOUtils.toString(
            getResourceAsStream("corruptedAccountsExpected.txt"), StandardCharsets.UTF_8);

    assertThat(outputFileString, Matchers.is(expectedFileString));
  }

  @Test
  void testFindNewEntries() throws Exception {
    File outputFile = Files.createTempFile(null, ".txt").toFile();
    int pageSize = 1;
    try (FileWriter fileWriter = new FileWriter(outputFile)) {
      DiffFinder.writeNewAccountsToFile(oldDbTemplate, newDbTemplate, pageSize, fileWriter);
    }

    String outputFileString;
    try (FileReader reader = new FileReader(outputFile)) {
      outputFileString = IOUtils.toString(reader);
    }

    String expectedFileString =
        IOUtils.toString(getResourceAsStream("newAccountsExpected.txt"), StandardCharsets.UTF_8);

    assertThat(outputFileString, Matchers.is(expectedFileString));
  }

  @Test
  void testFullReport() throws Exception {
    File outputFile = Files.createTempFile(null, ".txt").toFile();
    int pageSize = 1;
    try (FileWriter fileWriter = new FileWriter(outputFile)) {
      DiffFinder.writeDiffReportToFile(oldDbTemplate, newDbTemplate, pageSize, fileWriter);
    }

    String outputFileString;
    try (FileReader reader = new FileReader(outputFile)) {
      outputFileString = IOUtils.toString(reader);
    }

    String expectedFileString =
        IOUtils.toString(getResourceAsStream("fullReportExpected.txt"), StandardCharsets.UTF_8);

    assertThat(outputFileString, Matchers.is(expectedFileString));
  }

  @Test
  void testFullReportPageSize2() throws Exception {
    File outputFile = Files.createTempFile(null, ".txt").toFile();
    int pageSize = 2;
    try (FileWriter fileWriter = new FileWriter(outputFile)) {
      DiffFinder.writeDiffReportToFile(oldDbTemplate, newDbTemplate, pageSize, fileWriter);
    }

    String outputFileString;
    try (FileReader reader = new FileReader(outputFile)) {
      outputFileString = IOUtils.toString(reader);
    }

    String expectedFileString =
        IOUtils.toString(getResourceAsStream("fullReportExpected.txt"), StandardCharsets.UTF_8);

    assertThat(outputFileString, Matchers.is(expectedFileString));
  }

  @Test
  void testFullReportPageSizeGreaterThanResultCount() throws Exception {
    File outputFile = Files.createTempFile(null, ".txt").toFile();
    int pageSize = 100;
    try (FileWriter fileWriter = new FileWriter(outputFile)) {
      DiffFinder.writeDiffReportToFile(oldDbTemplate, newDbTemplate, pageSize, fileWriter);
    }

    String outputFileString;
    try (FileReader reader = new FileReader(outputFile)) {
      outputFileString = IOUtils.toString(reader);
    }

    String expectedFileString =
        IOUtils.toString(getResourceAsStream("fullReportExpected.txt"), StandardCharsets.UTF_8);

    assertThat(outputFileString, Matchers.is(expectedFileString));
  }

  private static InputStream getResourceAsStream(String resource) {
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
