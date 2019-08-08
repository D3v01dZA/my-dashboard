package com.altona.context;

import com.altona.security.Encryptor;
import com.altona.security.UserContext;
import com.altona.service.time.util.TimeConfig;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public interface EncryptionContext extends Context, TimeConfig, Encryptor {

    static EncryptionContext of(UserContext userContext, NamedParameterJdbcTemplate jdbcTemplate) {
        return new UserContextBasedEncryptionContext(userContext, jdbcTemplate);
    }

}
