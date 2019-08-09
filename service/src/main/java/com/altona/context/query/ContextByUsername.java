package com.altona.context.query;

import com.altona.context.Context;
import com.altona.context.SqlContext;
import com.altona.user.service.query.UserByUsername;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
public class ContextByUsername {

    @NonNull
    private SqlContext sqlContext;

    @NonNull
    private String username;

    public ContextByUsername(@NonNull SqlContext sqlContext, @NonNull Authentication authentication) {
        this(sqlContext, authentication.getName());
    }

    public Context execute() {
        return Context.of(sqlContext, new UserByUsername(sqlContext, username).execute());
    }

}
