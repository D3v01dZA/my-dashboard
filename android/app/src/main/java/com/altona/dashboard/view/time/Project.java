package com.altona.dashboard.view.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;

@Getter
public class Project {

    private int id;
    private String name;

    @JsonCreator
    Project(
            @JsonProperty(value = "id", required = true) int id,
            @JsonProperty(value = "name", required = true) String name
    ) {
        this.id = id;
        this.name = name;
    }


    static void setCurrentProject(TimeActivity timeActivity, int projectId) {
        timeActivity.projectSpinner().setSelection(getProjectIndex(timeActivity.currentProjects(), projectId));
    }

    private static int getProjectIndex(List<Project> projects, int projectId) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId() == projectId) {
                return i;
            }
        }
        throw new IllegalStateException("Couldn't find project id " + projectId + " + in " + projects);
    }

}
