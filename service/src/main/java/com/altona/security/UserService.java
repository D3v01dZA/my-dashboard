package com.altona.security;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
@AllArgsConstructor
public class UserService {

    private NamedParameterJdbcTemplate namedJdbcTemplate;

    public User getUser(Authentication authentication) {
        return getUserByUsername(authentication.getName())
                .orElseThrow(() -> new InsufficientAuthenticationException("Couldn't determine current user"));
    }

    public UserContext getUserContext(Authentication authentication, TimeZone timeZone) {
        return getUserByUsername(authentication.getName())
                .map(user -> new UserContext(user, timeZone))
                .orElseThrow(() -> new InsufficientAuthenticationException("Couldn't determine current user"));
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
