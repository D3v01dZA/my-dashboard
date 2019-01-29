package com.altona.service.synchronization.maconomy.model.init;

import com.altona.service.synchronization.maconomy.model.TimeData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Root {

    private TimeData data;

}
