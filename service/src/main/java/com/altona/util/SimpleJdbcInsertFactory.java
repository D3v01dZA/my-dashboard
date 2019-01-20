package com.altona.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

@Component
public class SimpleJdbcInsertFactory extends AbstractFactoryBean<SimpleJdbcInsert> {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public SimpleJdbcInsertFactory(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleJdbcInsert.class;
    }

    @Override
    protected SimpleJdbcInsert createInstance() {
        return new SimpleJdbcInsert(jdbcTemplate);
    }

}
