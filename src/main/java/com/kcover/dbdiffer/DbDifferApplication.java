package com.kcover.dbdiffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.File;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootApplication
public class DbDifferApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(DbDifferApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DbDifferApplication.class, args);
		System.out.println("Started");
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

		DiffFinder.writeMissingAccountsToFile(oldDbTemplate, newDbTemplate, 1000, new File("missing.txt"));
		DiffFinder.writeCorruptedAccountsToFile(oldDbTemplate, newDbTemplate, 1000, new File("corrupted.txt"));
	}


}
