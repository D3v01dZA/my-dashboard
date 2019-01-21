package com.altona.db.time.control;

import com.altona.db.time.Project;
import com.altona.db.time.Time;
import com.altona.db.time.TimeService;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TimeServiceRunningHelpers {

    static <T> T runningBreakAwareFunction(
            TimeService timeService,
            Project project,
            Function<Time, T> whenRunning,
            Supplier<T> whenNotRunning
    ) {
        return runningAwareFunction(timeService, Time.Type.BREAK, project, whenRunning, whenNotRunning);
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
