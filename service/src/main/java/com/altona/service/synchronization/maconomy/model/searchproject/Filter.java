package com.altona.service.synchronization.maconomy.model.searchproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter {

    private List<ProjectRecord> records;

    public Optional<ProjectData> getProjectData(String jobId) {
        for (ProjectRecord projectRecord : records) {
            ProjectData data = projectRecord.getData();
            if (jobId.equals(data.getJobnumber())) {
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

}
