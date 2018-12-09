package com.altona.db;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TableAccessor {

    private JdbcTemplate jdbcTemplate;
    private Map<Class<? extends Table<?>>, TableConfig<?>> tableConfigMap;

    public static TableAccessor of(@NonNull JdbcTemplate jdbcTemplate) {
        return new TableAccessor(jdbcTemplate, new ConcurrentHashMap<>());
    }

    public <T extends Table<K>, K> List<T> all(@NonNull Class<T> clazz) {
        return getConfig(clazz).all();
    }

    public <T extends Table<K>, K> void insert(@NonNull T value) {
        getConfig(value.getClass()).insert(value);
    }

    private <T extends Table<K>, K> TableConfig<T> getConfig(@NonNull Class<T> tableClazz) {
        return (TableConfig<T>) tableConfigMap.computeIfAbsent(tableClazz, clazz -> TableConfig.of(jdbcTemplate, tableClazz));
    }
    
}
