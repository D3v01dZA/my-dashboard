package com.altona.db;

import com.altona.KnownException;
import com.altona.UnknownException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class ColumnConfig<T extends Table<?>, C extends Value<V>, V> {

    @NonNull
    private JdbcTemplate jdbcTemplate;

    @NonNull
    private Class<T> tableClazz;
    @NonNull
    private Class<C> fieldClazz;
    @NonNull
    private Class<V> valueClazz;
    @NonNull
    private Field field;
    @Getter
    @NonNull
    private String columnName;

    static <T extends Table<?>, C extends Value<V>, V> ColumnConfig<T, ?, ?> of(@NonNull JdbcTemplate jdbcTemplate,
                                                                                @NonNull Class<T> tableClazz,
                                                                                @NonNull Field field) {
        field.setAccessible(true);
        Class<?> fieldClazz = field.getType();
        if (!Value.class.isAssignableFrom(fieldClazz)) {
            throw new KnownException(String.format("Field %s on table %s must be assignable from %s", field, tableClazz.getSimpleName(), Value.class.getName()));
        }
        Class<V> valueClass = (Class<V>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        return new ColumnConfig<>(jdbcTemplate, tableClazz, (Class<C>) fieldClazz, valueClass, field, field.getName());
    }

    boolean isKey() {
        return Key.class.isAssignableFrom(fieldClazz);
    }

    void createValue(T entry, ResultSet resultSet) throws SQLException {
        try {
            field.set(entry, new DatabaseRetrieved<>(extractValue(resultSet), this));
        } catch (IllegalAccessException e) {
            throw new UnknownException(e);
        }
    }

    Value<V> getValue(T entry) {
        try {
            Value<V> value = (Value<V>) field.get(entry);
            if (value == null) {
                throw new KnownException(String.format("Value for column %s on table %s was null", columnName, tableClazz.getSimpleName()));
            }
            return value;
        } catch (IllegalAccessException e) {
            throw new UnknownException(e);
        }
    }

    private V extractValue(ResultSet resultSet) throws SQLException {
        if (valueClazz.equals(Integer.class)) {
            int result = resultSet.getInt(columnName);
            handleNull(result);
            return valueClazz.cast(result);
        }
        if (valueClazz.equals(String.class)) {
            String result = resultSet.getString(columnName);
            handleNull(result);
            return valueClazz.cast(result);
        }
        throw new KnownException(String.format("Unable to handle type %s in table %s column %s", valueClazz.getName(), tableClazz.getSimpleName(), columnName));
    }

    private void handleNull(Object result) {
        if (result == null) {
            throw new KnownException(String.format("No value for column %s in table %s", columnName, tableClazz.getSimpleName()));
        }
    }

}
