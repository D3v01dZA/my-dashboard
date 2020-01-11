package com.altona.project.time;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.control.BreakStart;
import com.altona.project.time.control.BreakStop;
import com.altona.project.time.control.WorkStart;
import com.altona.project.time.control.WorkStop;
import com.altona.project.time.modify.EndTime;
import com.altona.project.time.view.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.Instant;

@AllArgsConstructor
public class RunningBreak implements CurrentTime {

    @NonNull
    private EncryptionContext encryptionContext;

    @NonNull
    private Project project;
    
    private int workId;

    @NonNull
    private Instant workStartTime;

    private int breakId;

    @NonNull
    private Instant breakStartTime;

    @Override
    public WorkStart startWork(Project project) {
        if (this.project.id() != project.id()) {
            return new OtherProjectTimeStarted(encryptionContext, project, this.project.id(), breakId);
        }
        return new BreakAlreadyStarted(project);
    }

    @Override
    public BreakStart startBreak(Project project) {
        if (this.project.id() != project.id()) {
            return new OtherProjectTimeStarted(encryptionContext, project, this.project.id(), breakId);
        }
        return new BreakAlreadyStarted(project);
    }

    @Override
    public BreakStop stopBreak(Project project) {
        if (this.project.id() != project.id()) {
            return new OtherProjectTimeStarted(encryptionContext, project, this.project.id(), breakId);
        }
        new EndTime(encryptionContext, workId).execute();
        return new BreakStopped();
    }

    @Override
    public WorkStop stopWork(Project project) {
        if (this.project.id() != project.id()) {
            return new OtherProjectTimeStarted(encryptionContext, project, this.project.id(), breakId);
        }
        new EndTime(encryptionContext, workId).execute();
        new EndTime(encryptionContext, breakId).execute();
        return new WorkStopped();
    }

    @Override
    public TimeStatusView asTimeStatusView() {
        return new TimeStatusView(
                TimeStatusView.Status.BREAK,
                breakId,
                project.id(),
                TimeUtil.difference(workStartTime, breakStartTime),
                TimeUtil.difference(breakStartTime, encryptionContext.now())
        );
    }

    @AllArgsConstructor
    private class BreakAlreadyStarted implements WorkStart, BreakStart {

        @NonNull
        private Project project;

        @Override
        public WorkStartView asWorkStartView() {
            return new WorkStartView(WorkStartView.Result.BREAK_ALREADY_STARTED, project.id(), null, breakId);
        }

        @Override
        public BreakStartView asBreakStartView() {
            return new BreakStartView(BreakStartView.Result.BREAK_ALREADY_STARTED, project.id(), null, breakId);
        }
    }

    @AllArgsConstructor
    private class BreakStopped implements BreakStop {

        @Override
        public BreakStopView asBreakStopView() {
            return new BreakStopView(BreakStopView.Result.BREAK_STOPPED, project.id(), null, breakId);
        }

    }

    @AllArgsConstructor
    private class WorkStopped implements WorkStop {

        @Override
        public WorkStopView asWorkStopView() {
            return new WorkStopView(WorkStopView.Result.WORK_AND_BREAK_STOPPED, project.id(), null, workId, breakId);
        }

    }


}
