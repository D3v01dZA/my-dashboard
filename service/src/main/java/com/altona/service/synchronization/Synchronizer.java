package com.altona.service.synchronization;

import com.altona.service.synchronization.model.SynchronizeResult;

public interface Synchronizer {

    int getSynchronizationId();

    SynchronizeResult synchronize();

}
