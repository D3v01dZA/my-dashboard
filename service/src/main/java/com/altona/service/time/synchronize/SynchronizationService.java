package com.altona.service.time.synchronize;

import com.altona.repository.db.time.project.Project;
import com.altona.security.UserContext;

public interface SynchronizationService {

    int getSynchronizationId();

    SynchronizationResult synchronize(UserContext userContext, Project project, SynchronizationCommand command);

}
