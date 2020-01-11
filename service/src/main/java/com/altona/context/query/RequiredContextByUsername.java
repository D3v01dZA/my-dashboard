package com.altona.context.query;

import com.altona.context.Context;
import com.altona.context.SqlContext;
import com.altona.user.query.RequiredUserByUsername;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
public class RequiredContextByUsername {

    @NonNull
    private SqlContext sqlContext;

    @NonNull
    private String username;

    public RequiredContextByUsername(@NonNull SqlContext sqlContext, @NonNull Authentication authentication) {
        this(sqlContext, authentication.getName());
    }

    public Context execute() {
        return Context.of(sqlContext, new RequiredUserByUsername(sqlContext, username).execute());
    }

}
