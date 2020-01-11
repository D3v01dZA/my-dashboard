package com.altona.project;

import com.altona.context.EncryptionContext;
import com.altona.context.SqlContext;
import com.altona.context.facade.ContextFacade;
import com.altona.project.query.ProjectsByUser;
import com.altona.context.TimeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.TimeZone;

@Service
public class ProjectFacade extends ContextFacade {

    @Autowired
    public ProjectFacade(SqlContext sqlContext, TimeInfo timeInfo) {
        super(sqlContext, timeInfo);
    }

    @Transactional(readOnly = true)
    public List<Project> projects(Authentication authentication, TimeZone timeZone) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectsByUser(encryptionContext).execute();
    }

    @Transactional
    public Project createProject(Authentication authentication,  TimeZone timeZone, UnsavedProject unsavedProject) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return unsavedProject.save(encryptionContext);
    }

}
