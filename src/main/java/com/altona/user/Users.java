package com.altona.user;

import com.altona.db.Key;
import com.altona.db.Table;
import com.altona.db.Value;

public class Users extends Table<Integer> {

    private Key<Integer> key;
    private Value<String> email;

    public Users() {

    }

    public Users(String email) {
        this.key = Key.createAutomatic();
        this.email = Value.create(email);
    }

    @Override
    protected Integer getKey() {
        return key.getValue();
    }
}
