package com.altona.db;

import com.altona.user.Users;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@ContextConfiguration
@EnableAutoConfiguration
@TestPropertySource("/application.properties")
@RunWith(SpringRunner.class)
public class TableAccessorTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void test() {
        TableAccessor systemUnderTest = TableAccessor.of(jdbcTemplate);
        List<Users> users = systemUnderTest.all(Users.class);
        Users toInsert = new Users("altona@gmail.com");
        systemUnderTest.insert(toInsert);
        List<Users> usersTwo = systemUnderTest.all(Users.class);
    }

}