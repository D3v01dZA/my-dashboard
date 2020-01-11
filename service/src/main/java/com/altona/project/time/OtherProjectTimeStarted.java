package com.altona.project.time;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.control.BreakStart;
import com.altona.project.time.control.BreakStop;
import com.altona.project.time.control.WorkStart;
import com.altona.project.time.control.WorkStop;
import com.altona.project.time.view.BreakStartView;
import com.altona.project.time.view.BreakStopView;
import com.altona.project.time.view.WorkStartView;
import com.altona.project.time.view.WorkStopView;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class OtherProjectTimeStarted implements WorkStart, BreakStart, BreakStop, WorkStop {

    @NonNull
    private EncryptionContext encryptionContext;

    @NonNull
    private Project requested;

    private int timeRunningProjectId;

    private int timeId;

    @Override
    public BreakStartView asBreakStartView() {
        return new BreakStartView(BreakStartView.Result.OTHER_PROJECT_TIME_RUNNING, requested.id(), timeRunningProjectId, timeId);
    }

    @Override
    public BreakStopView asBreakStopView() {
        return new BreakStopView(BreakStopView.Result.OTHER_PROJECT_TIME_RUNNING, requested.id(), timeRunningProjectId, timeId);
    }

    @Override
    public WorkStartView asWorkStartView() {
        return new WorkStartView(WorkStartView.Result.OTHER_PROJECT_TIME_RUNNING, requested.id(), timeRunningProjectId, timeId);
    }

    @Override
    public WorkStopView asWorkStopView() {
        return new WorkStopView(WorkStopView.Result.OTHER_PROJECT_TIME_RUNNING, requested.id(), timeRunningProjectId, timeId, null);
    }
}
