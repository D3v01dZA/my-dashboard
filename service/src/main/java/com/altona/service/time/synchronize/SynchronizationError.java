package com.altona.service.time.synchronize;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SynchronizationError {

    private int synchronizationId;
    private String detail;

}
