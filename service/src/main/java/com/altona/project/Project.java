package com.altona.project;

import com.altona.context.EncryptionContext;
import com.altona.project.view.ProjectView;
import com.altona.project.time.control.BreakStart;
import com.altona.project.time.control.BreakStop;
import com.altona.project.time.control.WorkStart;
import com.altona.project.time.control.WorkStop;
import com.altona.project.time.query.StartedTimeByUser;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class Project {

    @NonNull
    private EncryptionContext encryptionContext;

    private int id;

    @NonNull
    private String name;

    public WorkStart startWork() {
        return new StartedTimeByUser(encryptionContext).execute()
                .startWork(this);
    }

    public BreakStart startBreak() {
        return new StartedTimeByUser(encryptionContext).execute()
                .startBreak(this);
    }

    public BreakStop stopBreak() {
        return new StartedTimeByUser(encryptionContext).execute()
                .stopBreak(this);
    }

    public WorkStop stopWork() {
        return new StartedTimeByUser(encryptionContext).execute()
                .stopWork(this);
    }

    public int id() {
        return id;
    }

    public ProjectView asView() {
        return new ProjectView(id, name);
    }

}
