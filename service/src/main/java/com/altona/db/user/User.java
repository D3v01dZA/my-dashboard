package com.altona.db.user;

import java.util.Objects;

public class User {

    private int id;
    private String username;
    private String password;

    User(int id, String username, String password) {
        this.id = id;
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
