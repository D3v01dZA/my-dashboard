package com.altona.db;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor
class UserCreated<T> implements Value<T>, Key<T> {

    @NonNull
    private T value;

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public Optional<T> getValueForInsert() {
        return Optional.of(value);
    }
}
