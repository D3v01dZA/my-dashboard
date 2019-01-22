package com.altona.dashboard.service.time;

public class Project {

    private int id;
    private String name;

    Project(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
