package com.altona.context;

import com.altona.security.Encryptor;
import com.altona.service.time.util.TimeConfig;
import com.altona.user.service.UserContext;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Delegate;

@AllArgsConstructor
class UserContextBasedEncryptionContext implements EncryptionContext {

    @NonNull
    @Delegate(types = SqlContext.class)
    private SqlContext sqlContext;

    @NonNull
    @Delegate(types = {TimeConfig.class, Encryptor.class})
    private UserContext userContext;

    @Override
    public int getUserId() {
        return userContext.getId();
    }

}
