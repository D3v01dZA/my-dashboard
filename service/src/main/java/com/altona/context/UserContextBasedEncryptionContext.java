package com.altona.context;

import com.altona.user.UserContext;
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
    public int userId() {
        return userContext.getId();
    }

}
