package com.altona.context;

import com.altona.security.Encryptor;
import com.altona.security.UserContext;
import com.altona.service.time.util.TimeConfig;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Delegate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@AllArgsConstructor
class UserContextBasedEncryptionContext implements EncryptionContext {

    @NonNull
    @Delegate(types = {TimeConfig.class, Encryptor.class})
    private UserContext userContext;

    @NonNull
    @Delegate(types = NamedParameterJdbcOperations.class)
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public int getUserId() {
        return userContext.getId();
    }

}
