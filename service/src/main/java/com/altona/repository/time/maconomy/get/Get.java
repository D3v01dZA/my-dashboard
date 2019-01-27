package com.altona.repository.time.maconomy.get;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties
public class Get {

    private Panes panes;

    public TableRecord getTableRecord() {
        return panes.getTableRecord();
    }

    public CardRecord getCardRecord() {
        return panes.getCardRecord();
    }

}
