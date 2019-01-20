package com.altona.db.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class User {

    @Getter
    private int id;
    @NonNull
    @Getter
    private String username;
    @NonNull
    @Getter
    private String password;

}
