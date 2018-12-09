package com.altona.db;

public interface Value<T> extends InternalValue<T> {

    T getValue();

    static <T> Value<T> create(T value) {
        return new UserCreated<>(value);
    }

}
