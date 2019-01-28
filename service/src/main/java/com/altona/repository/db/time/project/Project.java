package com.altona.repository.db.time.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class Project {

    private int id;

    @NonNull
    private String name;

}
