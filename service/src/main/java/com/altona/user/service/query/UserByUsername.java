package com.altona.user.service.query;

import com.altona.context.SqlContext;
import com.altona.user.service.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
public class UserByUsername {

    @NonNull
    private SqlContext sqlContext;

    @NonNull
    private String username;

    public UserByUsername(@NonNull SqlContext sqlContext, @NonNull Authentication authentication) {
        this(sqlContext, authentication.getName());
    }

    public User execute() {
        return new OptionalUserByUsername(sqlContext, username).execute()
                .orElseThrow(() -> new InsufficientAuthenticationException("Couldn't determine current user"));
    }

}
