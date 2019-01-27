package com.altona.repository.time.maconomy.get;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Panes {

    private Card card;
    private Table table;

    public TableRecord getTableRecord() {
        return table.getTableRecord();
    }

    public CardRecord getCardRecord() {
        return card.getCardRecord();
    }

}
