package com.goldwatch.goldtracker.support;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlContainerConfig {

    static final String URL_PROPERTY = "it.mysql.url";

    static String externalUrl() {
        String url = System.getProperty(URL_PROPERTY);
        return url == null || url.isBlank() ? null : url;
    }

    /** Default: a throwaway MySQL 8 container shared by every test context; Spring stops it on shutdown. */
    @Bean
    @ServiceConnection
    @ConditionalOnExpression("'${it.mysql.url:}'.isEmpty()")
    MySQLContainer mysqlContainer() {
        return new MySQLContainer("mysql:8.4");
    }

    /** Alternative for machines without Docker: -Dit.mysql.url=... [-Dit.mysql.username=... -Dit.mysql.password=...]. */
    @Bean
    @ConditionalOnExpression("!'${it.mysql.url:}'.isEmpty()")
    DynamicPropertyRegistrar externalMySqlProperties() {
        return registry -> {
            registry.add("spring.datasource.url", MySqlContainerConfig::externalUrl);
            registry.add("spring.datasource.username", () -> System.getProperty("it.mysql.username", "root"));
            registry.add("spring.datasource.password", () -> System.getProperty("it.mysql.password", ""));
        };
    }
}
