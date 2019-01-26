package com.altona.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class User {

    @Getter
    private int id;

    @Getter
    @NonNull
    private String username;

    @Getter
    @NonNull
    private String password;

}
