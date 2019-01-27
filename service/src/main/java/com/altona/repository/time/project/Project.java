package com.altona.repository.time.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class Project {

    @Getter
    private int id;

    @Getter
    @NonNull
    private String name;

}
