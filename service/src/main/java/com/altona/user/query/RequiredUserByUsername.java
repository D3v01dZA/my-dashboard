package com.altona.user.query;

import com.altona.context.SqlContext;
import com.altona.user.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
public class RequiredUserByUsername {

    @NonNull
    private SqlContext sqlContext;

    @NonNull
    private String username;

    public RequiredUserByUsername(@NonNull SqlContext sqlContext, @NonNull Authentication authentication) {
        this(sqlContext, authentication.getName());
    }

    public User execute() {
        return new UserByUsername(sqlContext, username).execute()
                .orElseThrow(() -> new InsufficientAuthenticationException("Couldn't determine current user"));
    }

}
