package com.altona.db;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class DatabaseRetrieved<T extends Table<?>, C extends Value<V>, V> implements Value<V>, Key<V> {

    @NonNull
    private V value;
    @NonNull
    private ColumnConfig<T, C, V> columnConfig;

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public Optional<V> getValueForInsert() {
        return Optional.of(value);
    }
}
