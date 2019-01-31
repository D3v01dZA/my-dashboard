package com.altona.service.synchronization.maconomy.model.get;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties
public class Get {

    private Panes panes;

    public List<TableRecord> getTableRecords() {
        return panes.getTableRecords();
    }

    public CardRecord getCardRecord() {
        return panes.getCardRecord();
    }

    public CardData getCardData() {
        return getCardRecord().getData();
    }

}
