package com.altona.db.time;

import com.altona.db.user.User;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TimeServiceRunningHelpers {

    static <T> Optional<T> runningBreakAwareFunction(
            TimeService timeService,
            ProjectService projectService,
            User user,
            int projectId,
            BiFunction<Project, Time, T> whenRunning,
            Function<Project, T> whenNotRunning
    ) {
        return projectService.getProject(user, projectId)
                .map(project -> runningBreakAwareFunction(timeService, project, time -> whenRunning.apply(project, time), () -> whenNotRunning.apply(project)));
    }

    static <T> T runningBreakAwareFunction(
            TimeService timeService,
            Project project,
            Function<Time, T> whenRunning,
            Supplier<T> whenNotRunning
    ) {
        return runningAwareFunction(timeService, Time.Type.BREAK, project, whenRunning, whenNotRunning);
    }

    static <T> Optional<T> runningWorkAwareFunction(
            TimeService timeService,
            ProjectService projectService,
            User user,
            int projectId,
            BiFunction<Project, Time, T> whenRunning,
            Function<Project, T> whenNotRunning
    ) {
        return projectService.getProject(user, projectId)
                .map(project -> runningWorkAwareFunction(timeService, project, time -> whenRunning.apply(project, time), () -> whenNotRunning.apply(project)));
    }

    static <T> T runningWorkAwareFunction(
            TimeService timeService,
            Project project,
            Function<Time, T> whenRunning,
            Supplier<T> whenNotRunning
    ) {
        return runningAwareFunction(timeService, Time.Type.WORK, project, whenRunning, whenNotRunning);
    }

    static <T> T runningAwareFunction(
            TimeService timeService,
            Time.Type type,
            Project project,
            Function<Time, T> whenRunning,
            Supplier<T> whenNotRunning
    ) {
        Optional<Time> currentlyRunningOptional = timeService.getRunningProjectTime(project, type);
        if (currentlyRunningOptional.isPresent()) {
            return whenRunning.apply(currentlyRunningOptional.get());
        } else {
            return whenNotRunning.get();
        }
    }

}
