package com.altona.context;

import com.altona.user.service.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Delegate;

@AllArgsConstructor
class UserBasedContext implements Context {

    @NonNull
    @Delegate(types = SqlContext.class)
    private SqlContext sqlContext;

    @NonNull
    private User user;

    @Override
    public int getUserId() {
        return user.getId();
    }

}
