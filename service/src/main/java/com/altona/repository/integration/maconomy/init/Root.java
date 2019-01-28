package com.altona.repository.integration.maconomy.init;

import com.altona.repository.integration.maconomy.TimeData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Root {

    private TimeData data;

}
