package com.altona.service.synchronization.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SynchronizationError {

    private Synchronization synchronization;
    private String detail;

}
