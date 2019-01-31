package com.altona.service.synchronization.maconomy.model.searchjob;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Panes {

    private Filter filter;

    public Optional<JobData> getJobData(String projectId) {
        return filter.getJobData(projectId);
    }

}
