package com.altona.service.synchronization.maconomy.model.searchjob;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter {

    private List<JobRecord> records;

    public Optional<JobData> getJobData(String jobId) {
        for (JobRecord jobRecord : records) {
            JobData data = jobRecord.getData();
            if (jobId.equals(data.getTaskname())) {
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

}
