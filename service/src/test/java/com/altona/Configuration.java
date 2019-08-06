package com.altona;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import com.opentable.db.postgres.embedded.LiquibasePreparer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@TestConfiguration
public class Configuration {

    @Bean
    public DataSource dataSource(@Value("${spring.liquibase.change-log}") String liquibase) throws IOException, SQLException {
        EmbeddedPostgres db = EmbeddedPostgres.start();
        DataSource dataSource = db.getPostgresDatabase();
        new JdbcTemplate(dataSource).execute("CREATE EXTENSION pgcrypto");
        LiquibasePreparer.forClasspathLocation(liquibase.replace("classpath:", "")).prepare(dataSource);
        return dataSource;
    }

}
