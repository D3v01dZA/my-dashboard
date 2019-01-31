package com.altona.service.synchronization.maconomy.model.searchproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    private Panes panes;

    public Optional<ProjectData> getProjectData(String jobId) {
        return panes.getProjectData(jobId);
    }

}
