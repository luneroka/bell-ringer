package com.bell_ringer;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DbProbe {
    private static final Logger log = LoggerFactory.getLogger(DbProbe.class);
    private final JdbcTemplate jdbcTemplate;

    public DbProbe(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void verifyConnection() {
        Integer one = jdbcTemplate.queryForObject("select 1", Integer.class);
        log.info("Database connectivity probe result: {}", one);
    }
}
