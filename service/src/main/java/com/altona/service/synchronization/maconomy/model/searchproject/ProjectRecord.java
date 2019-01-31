package com.altona.service.synchronization.maconomy.model.searchproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectRecord {

    private ProjectData data;

}
