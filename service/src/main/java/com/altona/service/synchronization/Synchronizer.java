package com.altona.service.synchronization;

import com.altona.service.project.model.Project;
import com.altona.security.UserContext;
import com.altona.service.synchronization.model.SynchronizeCommand;
import com.altona.service.synchronization.model.SynchronizeResult;

public interface Synchronizer {

    int getSynchronizationId();

    SynchronizeResult synchronize(UserContext userContext, Project project, SynchronizeCommand command);

}
