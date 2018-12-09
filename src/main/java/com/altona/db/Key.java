package com.altona.db;

public interface Key<T> extends Value<T> {

    static <T> Key<T> create(T key) {
        return new UserCreated<>(key);
    }

    static <T> Key<T> createAutomatic() {
        return new DatabaseCreated<>();
    }

}
