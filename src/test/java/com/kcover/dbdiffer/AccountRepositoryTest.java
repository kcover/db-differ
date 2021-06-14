package com.kcover.dbdiffer;

import com.kcover.dbdiffer.jpa.Account;
import com.kcover.dbdiffer.jpa.AccountRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AccountRepositoryTest {

	@Autowired
	AccountRepository accountRepo;

	@Test
	void testRepoConfigured() {
		assertThat(accountRepo.findAll(), hasSize(3));
	}

}
