package com.altona.service.synchronization.maconomy.model.root;

import com.altona.service.synchronization.maconomy.model.MaconomyTimeData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Root {

    private MaconomyTimeData data;

}
