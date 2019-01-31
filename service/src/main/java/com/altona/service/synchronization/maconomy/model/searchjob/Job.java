package com.altona.service.synchronization.maconomy.model.searchjob;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {

    private Panes panes;

    public Optional<JobData> getJobData(String jobId) {
        return panes.getJobData(jobId);
    }

}
