package com.altona.project.time;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.control.BreakStart;
import com.altona.project.time.control.BreakStop;
import com.altona.project.time.control.WorkStart;
import com.altona.project.time.control.WorkStop;
import com.altona.project.time.modify.StartTime;
import com.altona.project.time.view.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class NoTime implements CurrentTime {

    @NonNull
    private EncryptionContext encryptionContext;

    @Override
    public WorkStart startWork(Project project) {
        int timeId = new StartTime(encryptionContext, project, TimeType.WORK).execute();
        return new WorkStarted(project, timeId);
    }

    @Override
    public BreakStart startBreak(Project project) {
        return new WorkNotStarted(project);
    }

    @Override
    public BreakStop stopBreak(Project project) {
        return new WorkNotStarted(project);
    }

    @Override
    public WorkStop stopWork(Project project) {
        return new WorkNotStarted(project);
    }

    @Override
    public TimeStatusView asTimeStatusView() {
        return new TimeStatusView(
                TimeStatusView.Status.NONE,
                null,
                null,
                null,
                null
        );
    }

    @AllArgsConstructor
    private static class WorkStarted implements WorkStart {

        @NonNull
        private Project project;

        private int timeId;

        @Override
        public WorkStartView asWorkStartView() {
            return new WorkStartView(WorkStartView.Result.WORK_STARTED, project.id(), null, timeId);
        }

    }

    @AllArgsConstructor
    private static class WorkNotStarted implements BreakStart, BreakStop, WorkStop {

        @NonNull
        private Project project;

        @Override
        public WorkStopView asWorkStopView() {
            return new WorkStopView(WorkStopView.Result.WORK_NOT_STARTED, project.id(), null, null, null);
        }

        @Override
        public BreakStartView asBreakStartView() {
            return new BreakStartView(BreakStartView.Result.WORK_NOT_STARTED, project.id(), null, null);
        }

        @Override
        public BreakStopView asBreakStopView() {
            return new BreakStopView(BreakStopView.Result.WORK_NOT_STARTED, project.id(), null, null);
        }
    }
}
