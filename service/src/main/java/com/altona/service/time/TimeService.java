package com.altona.service.time;

import com.altona.service.project.model.Project;
import com.altona.service.time.model.Time;
import com.altona.service.time.model.TimeCombination;
import com.altona.service.time.model.TimeType;
import com.altona.service.time.model.control.BreakStart;
import com.altona.service.time.model.control.BreakStop;
import com.altona.service.time.model.control.TimeStatus;
import com.altona.service.time.model.control.WorkStart;
import com.altona.service.time.model.control.WorkStop;
import com.altona.service.time.model.summary.SummaryConfiguration;
import com.altona.service.time.model.summary.SummaryCreator;
import com.altona.service.time.model.summary.SummaryFailure;
import com.altona.service.time.model.summary.TimeSummary;
import com.altona.service.time.util.SingleTimeStatusCollector;
import com.altona.service.time.util.TimeConfig;
import com.altona.service.time.util.TimeInfo;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Service
@AllArgsConstructor
public class TimeService {

    private TimeRepository timeRepository;

    public WorkStart startProjectWork(List<Project> projects, Project project, TimeInfo timeInfo) {
        log.info("Starting work");
        return timeStatusInternal(projects, timeInfo)
                .map(timeStatus -> WorkStart.alreadyStarted(timeStatus.getProjectId().get(), timeStatus.getTimeId().get()))
                .orElseGet(
                        () -> runningWorkAwareFunction(
                                project,
                                runningWork -> WorkStart.alreadyStarted(project.getId(), runningWork.getId()),
                                () -> WorkStart.started(project.getId(), timeRepository.startTime(project.getId(), TimeType.WORK, timeInfo))
                        )
                );
    }

    public WorkStop endProjectWork(Project project, TimeInfo timeInfo) {
        log.info("Stopping work");
        return runningWorkAwareFunction(
                project,
                runningWork -> {
                    Time endedWork = stopTime(project, runningWork, timeInfo);
                    return runningBreakAwareFunction(
                            project,
                            (runningBreak) -> WorkStop.ended(endedWork.getId(), stopTime(project, runningBreak, timeInfo).getId()),
                            () -> WorkStop.ended(endedWork.getId())
                    );
                },
                WorkStop::notStarted
        );
    }

    public BreakStart startProjectBreak(Project project, TimeInfo timeInfo) {
        log.info("Starting break");
        return runningWorkAwareFunction(
                project,
                runningWork -> runningBreakAwareFunction(
                        project,
                        runningBreak -> BreakStart.breakAlreadyStarted(runningBreak.getId()),
                        () -> BreakStart.started(timeRepository.startTime(project.getId(), TimeType.BREAK, timeInfo))
                ),
                BreakStart::workNotStarted
        );
    }

    public BreakStop endProjectBreak(Project project, TimeInfo timeInfo) {
        log.info("Stopping break");
        return runningBreakAwareFunction(
                project,
                runningBreak -> BreakStop.stopped(stopTime(project, runningBreak, timeInfo).getId()),
                () -> runningWorkAwareFunction(
                        project,
                        runningWork -> BreakStop.breakNotStarted(),
                        BreakStop::workNotStarted
                )
        );
    }

    public TimeStatus timeStatus(List<Project> projects, TimeInfo timeInfo) {
        return timeStatusInternal(projects, timeInfo)
                .orElseGet(TimeStatus::none);
    }

    public Result<TimeSummary, SummaryFailure> summary(TimeConfig timeConfig, Project project, SummaryConfiguration configuration) {
        log.info("Summarizing {}", configuration);
        List<Time> times = timeRepository
                .timeListBetween(
                        project.getId(),
                        timeConfig.unlocalize(configuration.getFrom()),
                        timeConfig.unlocalize(configuration.getTo())
                );
        return new SummaryCreator(timeConfig, configuration).create(timeConfig, TimeCombination.createCombinations(times));
    }

    private Optional<TimeStatus> timeStatusInternal(List<Project> projects, TimeInfo timeInfo) {
        return projects.stream()
                .map(project -> timeStatus(project, timeInfo))
                .collect(SingleTimeStatusCollector.INSTANCE);
    }

    private TimeStatus timeStatus(Project project, TimeInfo timeInfo) {
        return runningWorkAwareFunction(
                project,
                runningWork -> {
                    LocalTime breakTime = timeRepository.timesFromDate(project.getId(), runningWork.getStart())
                            .stream()
                            .map(time -> {
                                Assert.isTrue(time.getType() == TimeType.BREAK, "All times after a non-ended work should be breaks");
                                return time.time(timeInfo.now());
                            })
                            .reduce(
                                    LocalTime.of(0, 0),
                                    (timeOne, timeTwo) -> timeOne.plus(timeTwo.toNanoOfDay(), ChronoUnit.NANOS)
                            );
                    LocalTime workTime = runningWork.time(timeInfo.now()).minus(breakTime.toNanoOfDay(), ChronoUnit.NANOS);
                    return runningBreakAwareFunction(project,
                            runningBreak -> TimeStatus.onBreak(project, runningBreak, workTime, breakTime),
                            () -> TimeStatus.atWork(project, runningWork, workTime, breakTime)
                    );
                },
                TimeStatus::none
        );
    }

    private Time stopTime(Project project, Time time, TimeInfo timeInfo) {
        timeRepository.stopTime(project.getId(), time.getId(), timeInfo);
        return timeRepository.time(project.getId(), time.getId()).get();
    }

    private <T> T runningBreakAwareFunction(Project project, Function<Time, T> whenRunning, Supplier<T> whenNotRunning) {
        return runningAwareFunction(TimeType.BREAK, project, whenRunning, whenNotRunning);
    }

    private <T> T runningWorkAwareFunction(Project project, Function<Time, T> whenRunning, Supplier<T> whenNotRunning) {
        return runningAwareFunction(TimeType.WORK, project, whenRunning, whenNotRunning);
    }

    private <T> T runningAwareFunction(TimeType type, Project project, Function<Time, T> whenRunning, Supplier<T> whenNotRunning) {
        Optional<Time> currentlyRunningOptional = timeRepository.timeWithNullEnd(project.getId(), type);
        if (currentlyRunningOptional.isPresent()) {
            return whenRunning.apply(currentlyRunningOptional.get());
        } else {
            return whenNotRunning.get();
        }
    }

}
