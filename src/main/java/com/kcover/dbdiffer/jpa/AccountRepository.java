package com.kcover.dbdiffer.jpa;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * A JPA repository auto-implemented by spring. When the application starts, spring-data-jpa will scan for interfaces
 * extending {@link JpaRepository} in packages specified by the {@link org.springframework.context.annotation.ComponentScan}
 * annotation. For the interfaces it finds, it creates an implementation based on the ID and Value types given, and puts
 * it on the application context as a bean.
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {}