package com.altona.db.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
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
