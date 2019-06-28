package com.altona.service.synchronization;

import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.util.Result;

public interface Synchronizer {

    Synchronization getSynchronization();

    Result<Screenshot, String> synchronize(SynchronizationAttempt attempt);

}
