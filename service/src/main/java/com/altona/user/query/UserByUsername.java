package com.altona.user.query;

import com.altona.context.SqlContext;
import com.altona.user.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class UserByUsername {

    @NonNull
    private SqlContext sqlContext;

    @NonNull
    private String username;

    public Optional<User> execute() {
        List<User> users = sqlContext.query(
                "SELECT id, username, password, salt FROM users WHERE username = :username",
                new MapSqlParameterSource("username", username),
                (rs, rn) -> new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("salt")
                )
        );
        if (users.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

}
