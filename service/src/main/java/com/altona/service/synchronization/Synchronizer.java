package com.altona.service.synchronization;

import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;

public interface Synchronizer {

    Synchronization getSynchronization();

    Screenshot synchronize(SynchronizationAttempt attempt) throws SynchronizationException;

}
