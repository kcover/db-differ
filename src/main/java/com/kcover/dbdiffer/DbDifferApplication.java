package com.kcover.dbdiffer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DbDifferApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbDifferApplication.class, args);
		System.out.println("Started");
	}

}
