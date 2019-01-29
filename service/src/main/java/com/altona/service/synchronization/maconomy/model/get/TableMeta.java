package com.altona.service.synchronization.maconomy.model.get;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableMeta {

    private int rowNumber;
    private String concurrencyControl;

}
