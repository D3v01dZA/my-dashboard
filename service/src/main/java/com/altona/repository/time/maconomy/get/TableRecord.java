package com.altona.repository.time.maconomy.get;

import com.altona.repository.time.maconomy.TimeData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableRecord {

    private TableMeta meta;
    private TimeData data;

}
