package com.altona.service.synchronization.maconomy.model.get;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Panes {

    private Card card;
    private Table table;

    public List<TableRecord> getTableRecords() {
        return table.getRecords();
    }

    public CardRecord getCardRecord() {
        return card.getCardRecord();
    }

}
