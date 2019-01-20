package com.altona.db.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Project {

    private int id;
    private String name;

    @JsonCreator
    Project(@JsonProperty("id") int id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
