package com.altona.service.synchronization.maconomy.model.init;

import com.altona.service.synchronization.maconomy.model.MaconomyTimeData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Init {

    private MaconomyTimeData data;

}