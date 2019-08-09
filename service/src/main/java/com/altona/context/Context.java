package com.altona.context;

import com.altona.user.service.User;

public interface Context extends SqlContext {

    static Context of(SqlContext sqlContext, User user) {
        return new UserBasedContext(sqlContext, user);
    }

    int getUserId();

}
