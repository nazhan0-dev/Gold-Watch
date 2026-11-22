package com.goldwatch.goldtracker.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Boots the whole application against a real MySQL 8 database: a Testcontainers MySQL container by default,
 * or an existing server when {@code -Dit.mysql.url=jdbc:mysql://...} is given (see {@link MySqlContainerConfig}).
 * Skipped automatically when neither Docker nor an external URL is available; CI always has Docker.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MySqlAvailableCondition.class)
@SpringBootTest
@Import(MySqlContainerConfig.class)
@TestPropertySource(properties = "goldtracker.scheduler.enabled=false")
public @interface MySqlIntegrationTest {
}
