package com.altona.db;

import com.altona.KnownException;

import java.util.Optional;

class DatabaseCreated<T> implements Key<T> {

    @Override
    public T getValue() {
        throw new KnownException("A database value can only be retrieved after persisting the entity");
    }

    @Override
    public Optional<T> getValueForInsert() {
        return Optional.empty();
    }
}
