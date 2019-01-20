package com.altona.db.time;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class Time {

    private int id;
    private Type type;
    private Date start;
    private Date end;

    Time(int id, String type, Date start, Date end) {
        this.id = id;
        this.type = Type.valueOf(Objects.requireNonNull(type));
        this.start = Objects.requireNonNull(start);
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Date getStart() {
        return start;
    }

    public Optional<Date> getEnd() {
        return Optional.ofNullable(end);
    }
}
