package com.altona.dashboard.service.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Project {

    private int id;
    private String name;

    @JsonCreator
    public Project(
            @JsonProperty(value = "id", required = true) int id,
            @JsonProperty(value = "name", required = true) String name
    ) {
        this.id = id;
        this.name = name;
    }

}
