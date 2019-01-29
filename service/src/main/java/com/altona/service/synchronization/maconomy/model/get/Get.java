package com.altona.service.synchronization.maconomy.model.get;

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

    public CardData getCardData() {
        return getCardRecord().getData();
    }

}
