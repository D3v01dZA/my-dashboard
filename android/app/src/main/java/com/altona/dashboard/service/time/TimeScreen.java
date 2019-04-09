package com.altona.dashboard.service.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

public class TimeScreen {

    private Project project;
    private List<Project> projects;
    private TimeStatus timeStatus;
    private TimeSummary timeSummary;

    @JsonCreator
    public TimeScreen(
            @JsonProperty(value = "project") Project project,
            @JsonProperty(value = "projects", required = true) List<Project> projects,
            @JsonProperty(value = "timeStatus", required = true) TimeStatus timeStatus,
            @JsonProperty(value = "timeSummary") TimeSummary timeSummary
    ) {
        this.project = project;
        this.projects = projects;
        this.timeStatus = timeStatus;
        this.timeSummary = timeSummary;
    }

    public Optional<Project> getProject() {
        return Optional.ofNullable(project);
    }

    public List<Project> getProjects() {
        return projects;
    }

    public TimeStatus getTimeStatus() {
        return timeStatus;
    }

    public Optional<TimeSummary> getTimeSummary() {
        return Optional.ofNullable(timeSummary);
    }
}
