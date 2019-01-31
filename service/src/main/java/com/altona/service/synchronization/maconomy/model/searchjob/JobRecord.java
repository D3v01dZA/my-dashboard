package com.altona.service.synchronization.maconomy.model.searchjob;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobRecord {

    private JobData data;

}
