package com.altona.service.synchronization.maconomy.model.get;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CardMeta {

    private int rowNumber;
    private String concurrencyControl;

}
