package com.altona.project.time;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.control.BreakStart;
import com.altona.project.time.control.BreakStop;
import com.altona.project.time.control.WorkStart;
import com.altona.project.time.control.WorkStop;
import com.altona.project.time.modify.StartTime;
import com.altona.project.time.modify.EndTime;
import com.altona.project.time.view.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.Instant;

@AllArgsConstructor
public class RunningWork implements CurrentTime {

    @NonNull
    private EncryptionContext encryptionContext;

    @NonNull
    private Project project;

    private int id;

    @NonNull
    private Instant startTime;

    @Override
    public WorkStart startWork(Project project) {
        if (this.project.id() != project.id()) {
            return new OtherProjectTimeStarted(encryptionContext, project, this.project.id(), id);
        }
        return new WorkAlreadyStarted(project);
    }

    @Override
    public BreakStart startBreak(Project project) {
        if (this.project.id() != project.id()) {
            return new OtherProjectTimeStarted(encryptionContext, project, this.project.id(), id);
        }
        int breakId = new StartTime(encryptionContext, project, TimeType.BREAK).execute();
        return new BreakStarted(breakId);
    }

    @Override
    public BreakStop stopBreak(Project project) {
        if (this.project.id() != project.id()) {
            return new OtherProjectTimeStarted(encryptionContext, project, this.project.id(), id);
        }
        return new BreakNotStarted(project);
    }

    @Override
    public WorkStop stopWork(Project project) {
        if (this.project.id() != project.id()) {
            return new OtherProjectTimeStarted(encryptionContext, project, this.project.id(), id);
        }
        new EndTime(encryptionContext, id).execute();
        return new WorkStopped();
    }

    @Override
    public TimeStatusView asTimeStatusView() {
        return new TimeStatusView(
                TimeStatusView.Status.WORK,
                id,
                project.id(),
                TimeUtil.difference(startTime, encryptionContext.now()),
                TimeUtil.ZERO
        );
    }

    @AllArgsConstructor
    private class WorkAlreadyStarted implements WorkStart {

        @NonNull
        private Project project;

        @Override
        public WorkStartView asWorkStartView() {
            return new WorkStartView(WorkStartView.Result.WORK_ALREADY_STARTED, project.id(), null, id);
        }
    }

    @AllArgsConstructor
    private class BreakStarted implements BreakStart {

        private int breakId;

        @Override
        public BreakStartView asBreakStartView() {
            return new BreakStartView(BreakStartView.Result.BREAK_STARTED, project.id(), null, breakId);
        }

    }

    @AllArgsConstructor
    private class BreakNotStarted implements BreakStop {

        @NonNull
        private Project project;

        @Override
        public BreakStopView asBreakStopView() {
            return new BreakStopView(BreakStopView.Result.BREAK_NOT_STARTED, project.id(), null, id);
        }

    }

    @AllArgsConstructor
    private class WorkStopped implements WorkStop {

        @Override
        public WorkStopView asWorkStopView() {
            return new WorkStopView(WorkStopView.Result.WORK_STOPPED, project.id(), null, id, null);
        }

    }

}
