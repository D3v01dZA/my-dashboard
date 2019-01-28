package com.altona.repository.integration.maconomy.get;

import com.altona.repository.integration.maconomy.MaconomyException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {

    private List<CardRecord> records;

    public CardRecord getCardRecord() {
        if (records.size() != 1) {
            throw new MaconomyException("Expected exactly 1 record in current time response but got multiple");
        }
        return records.get(0);
    }

}
