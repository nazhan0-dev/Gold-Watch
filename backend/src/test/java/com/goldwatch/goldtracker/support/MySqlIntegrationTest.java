package com.goldwatch.goldtracker.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Boots the whole application against a real MySQL 8 container.
 * Skipped automatically when no Docker daemon is available (CI always has one).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Import(MySqlContainerConfig.class)
@TestPropertySource(properties = "goldtracker.scheduler.enabled=false")
public @interface MySqlIntegrationTest {
}
