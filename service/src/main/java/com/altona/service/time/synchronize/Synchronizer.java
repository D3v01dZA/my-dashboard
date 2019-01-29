package com.altona.service.time.synchronize;

import com.altona.repository.db.time.project.Project;
import com.altona.security.UserContext;

public interface Synchronizer {

    int getSynchronizationId();

    SynchronizeResult synchronize(UserContext userContext, Project project, SynchronizeCommand command);

}
