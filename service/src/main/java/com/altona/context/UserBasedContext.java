package com.altona.context;

import com.altona.security.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Delegate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@AllArgsConstructor
class UserBasedContext implements Context {

    @NonNull
    private User user;

    @NonNull
    @Delegate(types = NamedParameterJdbcOperations.class)
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public int getUserId() {
        return user.getId();
    }

}
