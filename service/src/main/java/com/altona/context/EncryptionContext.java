package com.altona.context;

import com.altona.user.UserContext;

public interface EncryptionContext extends Context, TimeConfig, Encryptor {

    static EncryptionContext of(SqlContext sqlContext, UserContext userContext) {
        return new UserContextBasedEncryptionContext(sqlContext, userContext);
    }

}
