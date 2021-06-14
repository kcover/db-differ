package com.kcover.dbdiffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/old");
		dataSource.setUsername("old");
		dataSource.setPassword("hehehe");
		JdbcTemplate template = new JdbcTemplate(dataSource);

		Integer count = template.queryForObject("SELECT COUNT(id) FROM accounts", Integer.class);
		LOGGER.info("COUNT WAS: " + count);

		List<Account> accountMap =
				template
				.queryForList("SELECT * FROM accounts ORDER BY id LIMIT 5")
				.stream().map(DbDifferApplication::mapToAccount)
				.collect(Collectors.toList());
		LOGGER.info("FIRST FIVE WERE: " + accountMap);
	}

	public static Account mapToAccount(Map<String, Object> map){
		var id = map.get("id");
		var name = map.get("name");
		var email = map.get("email");
		if(id instanceof String && name instanceof String && email instanceof String) {
			return new Account((String) id,(String) name,(String) email);
		} else {
			throw new RuntimeException("Failed to convert map to Account: " + map);
		}
	}


}
