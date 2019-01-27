package com.altona.repository.time.maconomy.init;

import com.altona.repository.time.maconomy.TimeData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Root {

    private TimeData data;

}
