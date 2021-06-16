package com.kcover.dbdiffer;

import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DbDifferApplication {
  private static final Logger LOGGER = LoggerFactory.getLogger(DbDifferApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(DbDifferApplication.class, args);
    DriverManagerDataSource oldDbDataSource = new DriverManagerDataSource();
    oldDbDataSource.setDriverClassName("org.postgresql.Driver");
    oldDbDataSource.setUrl("jdbc:postgresql://localhost:5432/old");
    oldDbDataSource.setUsername("old");
    oldDbDataSource.setPassword("hehehe");
    JdbcTemplate oldDbTemplate = new JdbcTemplate(oldDbDataSource);

    DriverManagerDataSource newDbDataSource = new DriverManagerDataSource();
    newDbDataSource.setDriverClassName("org.postgresql.Driver");
    newDbDataSource.setUrl("jdbc:postgresql://localhost:5433/new");
    newDbDataSource.setUsername("new");
    newDbDataSource.setPassword("hahaha");
    JdbcTemplate newDbTemplate = new JdbcTemplate(newDbDataSource);

    LOGGER.info("Writing report...");
    try (FileWriter reportWriter = new FileWriter("report.txt")) {
      DiffFinder.writeDiffReportToFile(oldDbTemplate, newDbTemplate, 1000, reportWriter);
    } catch (IOException e) {
      throw new RuntimeException("IO error occurred while creating filewriter to write report.", e);
    }
  }
}
