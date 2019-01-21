package com.altona.db.time;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Date;
import java.util.Optional;

@AllArgsConstructor
public class Time {

    @Getter
    private int id;

    @Getter
    @NonNull
    private Type type;

    @Getter
    @NonNull
    private Date start;

    private Date end;

    Time(int id, String type, Date start, Date end) {
        this(id, Type.valueOf(type), start, end);
    }

    public Optional<Date> getEnd() {
        return Optional.ofNullable(end);
    }

    public enum Type {
        WORK,
        BREAK;
    }
}
