package com.altona.context;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Delegate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
class UserlessContext implements SqlContext {

    @NonNull
    @Delegate(types = NamedParameterJdbcOperations.class)
    private NamedParameterJdbcOperations jdbcOperations;

}
