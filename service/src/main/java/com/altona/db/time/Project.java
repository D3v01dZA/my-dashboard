package com.altona.db.time;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class Project {

    @Getter
    private int id;
    @NonNull
    @Getter
    private String name;

}
