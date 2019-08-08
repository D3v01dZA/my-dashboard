package com.altona.context;

import com.altona.security.User;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public interface Context extends NamedParameterJdbcOperations {

    static Context of(User user, NamedParameterJdbcTemplate jdbcTemplate) {
        return new UserBasedContext(user, jdbcTemplate);
    }

    int getUserId();

}
