package com.altona.service.synchronization.maconomy.model.get;

import com.altona.service.synchronization.maconomy.model.MaconomyTimeData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableRecord {

    private TableMeta meta;
    private MaconomyTimeData data;

}
