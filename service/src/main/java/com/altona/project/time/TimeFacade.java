package com.altona.project.time;

import com.altona.context.EncryptionContext;
import com.altona.context.SqlContext;
import com.altona.context.TimeInfo;
import com.altona.context.facade.ContextFacade;
import com.altona.project.Project;
import com.altona.project.query.ProjectById;
import com.altona.project.time.control.BreakStart;
import com.altona.project.time.control.BreakStop;
import com.altona.project.time.control.WorkStart;
import com.altona.project.time.control.WorkStop;
import com.altona.project.time.query.StartedTimeByUser;
import com.altona.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.TimeZone;

@Service
public class TimeFacade extends ContextFacade {

    @Autowired
    public TimeFacade(SqlContext sqlContext, TimeInfo timeInfo) {
        super(sqlContext, timeInfo);
    }

    @Transactional
    public Optional<WorkStart> startWork(Authentication authentication, TimeZone timeZone, int projectId) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .map(Project::startWork);
    }

    @Transactional
    public Optional<BreakStart> startBreak(Authentication authentication, TimeZone timeZone, int projectId) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .map(Project::startBreak);
    }

    @Transactional
    public Optional<WorkStop> endWork(Authentication authentication, TimeZone timeZone, int projectId) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .map(Project::stopWork);
    }

    @Transactional
    public Optional<BreakStop> endBreak(Authentication authentication, TimeZone timeZone, int projectId) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .map(Project::stopBreak);
    }

    @Transactional(readOnly = true)
    public CurrentTime currentTime(Authentication authentication, TimeZone timeZone) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new StartedTimeByUser(encryptionContext).execute();
    }

    @Transactional(readOnly = true)
    public Optional<Result<TimeSummary, String>> summary(Authentication authentication, TimeZone timeZone, int projectId, SummaryType summaryType) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .map(project -> summaryType.createSelection(encryptionContext, project).execute());
    }

}
