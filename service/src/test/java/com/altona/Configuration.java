package com.altona;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import com.opentable.db.postgres.embedded.LiquibasePreparer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public DataSource dataSource(@Value("${spring.liquibase.change-log}") String liquibase) throws IOException, SQLException {
        EmbeddedPostgres db = EmbeddedPostgres.start();
        DataSource dataSource = db.getPostgresDatabase();
        new JdbcTemplate(dataSource).execute("CREATE EXTENSION pgcrypto");
        LiquibasePreparer.forClasspathLocation(liquibase.replace("classpath:", "")).prepare(dataSource);
        SimpleJdbcInsert userInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
        userInsert.execute(new MapSqlParameterSource()
                .addValue("username", "test")
                .addValue("password", "$2a$10$22t/X7uEYPQxSS7C9aOBYeaNGy4gYzcIX8X/GbuQZ82i6BG/lnR2a")
                .addValue("salt", "715814fc-98db-49ab-af16-69617351d382"));
        return dataSource;
    }

}
