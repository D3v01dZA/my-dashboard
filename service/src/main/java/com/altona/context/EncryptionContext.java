package com.altona.context;

import com.altona.security.Encryptor;
import com.altona.service.time.util.TimeConfig;
import com.altona.user.service.UserContext;

public interface EncryptionContext extends Context, TimeConfig, Encryptor {

    static EncryptionContext of(SqlContext sqlContext, UserContext userContext) {
        return new UserContextBasedEncryptionContext(sqlContext, userContext);
    }

}
