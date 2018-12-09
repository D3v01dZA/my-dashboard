package com.altona.db;

import com.altona.KnownException;
import com.altona.UnknownException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class TableConfig<T extends Table<?>> {

    @NonNull
    private JdbcTemplate jdbcTemplate;
    @NonNull
    private SimpleJdbcInsert jdbcInsert;

    @NonNull
    private Class<T> tableClazz;
    @NonNull
    private ColumnConfig<T, ?, ?> keyColumn;
    @NonNull
    private List<ColumnConfig<T, ?, ?>> nonKeyColumns;
    @NonNull
    private List<ColumnConfig<T, ?, ?>> allColumns;

    static <T extends Table<?>> TableConfig<T> of(@NonNull JdbcTemplate jdbcTemplate, @NonNull Class<T> tableClazz) {
        try {
            // Ensure there's a no-args constructor
            tableClazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new KnownException(String.format("Default constructor required for %s", tableClazz.getName()));
        }
        Class<?> superClazz = tableClazz;
        List<Field> allFields = new ArrayList<>();
        while(superClazz != null) {
            allFields.addAll(Arrays.asList(superClazz.getDeclaredFields()));
            superClazz = superClazz.getSuperclass();
        }
        Set<String> fieldNames = new HashSet<>(allFields.size());
        ColumnConfig<T, ?, ?> keyColumn = null;
        List<ColumnConfig<T, ?, ?>> nonKeyColumns = new ArrayList<>(allFields.size());
        for (Field field : allFields) {
            ColumnConfig<T, ?, ?> columnConfig = ColumnConfig.of(jdbcTemplate, tableClazz, field);
            if (columnConfig.isKey()) {
                if (keyColumn != null) {
                    throw new KnownException(String.format("Multiple key columns defined for table %s, trying to define %s while %s is already defined", tableClazz.getName(), columnConfig.getColumnName(), keyColumn.getColumnName()));
                } else {
                    keyColumn = columnConfig;
                }
            } else {
                nonKeyColumns.add(columnConfig);
            }
            fieldNames.add(columnConfig.getColumnName());
        }
        if (fieldNames.size() != allFields.size()) {
            throw new KnownException(String.format("Duplicate column name found for table %s in %s", tableClazz.getName(), allFields.stream().map(Field::getName).collect(toList())));
        }
        if (keyColumn == null) {
            throw new KnownException(String.format("No key column defined for table %s with columns %s", tableClazz.getName(), allFields.stream().map(Field::getName).collect(toList())));
        }
        List<ColumnConfig<T, ?, ?>> allColumns = new ArrayList<>();
        allColumns.add(keyColumn);
        allColumns.addAll(nonKeyColumns);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource()).withTableName(tableClazz.getSimpleName());
        return new TableConfig<>(jdbcTemplate, simpleJdbcInsert, tableClazz, keyColumn, nonKeyColumns, allColumns);
    }

    List<T> all() {
        String columns = allColumns.stream().map(ColumnConfig::getColumnName).collect(Collectors.joining(","));
        return jdbcTemplate.query("SELECT " + columns + " FROM " + getTableName(), (resultSet, rowNumber) -> createObject(resultSet));
    }

    void insert(T object) {
        Map<String, Object> values = new HashMap<>();
        for (ColumnConfig<T, ?, ?> columnConfig : allColumns) {
            columnConfig.getValue(object).getValueForInsert()
                    .ifPresent(insertValue -> values.put(columnConfig.getColumnName(), insertValue));
        }
        jdbcInsert.execute(values);
    }

    private String getTableName() {
        return tableClazz.getSimpleName();
    }

    private T createObject(ResultSet resultSet) {
        try {
            T object = tableClazz.getConstructor().newInstance();
            for (ColumnConfig<T, ?, ?> columnConfig : allColumns) {
                columnConfig.createValue(object, resultSet);
            }
            return object;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SQLException e) {
            throw new UnknownException(e);
        }
    }

}
