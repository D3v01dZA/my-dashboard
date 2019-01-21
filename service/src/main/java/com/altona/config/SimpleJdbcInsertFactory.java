package com.altona.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SimpleJdbcInsertFactory extends AbstractFactoryBean<SimpleJdbcInsert> {

    private JdbcTemplate jdbcTemplate;

    @Override
    public Class<?> getObjectType() {
        return SimpleJdbcInsert.class;
    }

    @Override
    protected SimpleJdbcInsert createInstance() {
        return new SimpleJdbcInsert(jdbcTemplate);
    }

}
