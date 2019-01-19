package com.altona.db.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public UserService(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    public Optional<User> getUserByUsername(String username) {
        List<User> users = namedJdbcTemplate.query(
                "SELECT id, username, password FROM users WHERE username = :username",
                new MapSqlParameterSource("username", username),
                (rs, rn) -> new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"))
        );
        if (users.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

}
