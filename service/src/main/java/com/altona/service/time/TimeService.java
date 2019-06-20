package com.altona.service.time;

import com.altona.security.UserContext;
import com.altona.service.broadcast.BroadcastService;
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
    private BroadcastService broadcastService;

    public WorkStart startProjectWork(List<Project> projects, Project project, UserContext user) {
        log.info("Starting work");
        return timeStatusInternal(projects, user)
                .map(timeStatus -> WorkStart.alreadyStarted(timeStatus.getTimeId().get()))
                .orElseGet(
                        () -> runningWorkAwareFunction(
                                project,
                                runningWork -> WorkStart.alreadyStarted(runningWork.getId()),
                                () -> {
                                    WorkStart started = WorkStart.started(timeRepository.startTime(project.getId(), TimeType.WORK, user));
                                    broadcastService.broadcast(user);
                                    return started;
                                }
                        )
                );
    }

    public WorkStop endProjectWork(Project project, UserContext user) {
        log.info("Stopping work");
        return runningWorkAwareFunction(
                project,
                runningWork -> {
                    Time endedWork = stopTime(project, runningWork, user);
                    return runningBreakAwareFunction(
                            project,
                            (runningBreak) -> {
                                WorkStop ended = WorkStop.ended(endedWork.getId(), stopTime(project, runningBreak, user).getId());
                                broadcastService.broadcast(user);
                                return ended;
                            },
                            () -> {
                                WorkStop ended = WorkStop.ended(endedWork.getId());
                                broadcastService.broadcast(user);
                                return ended;
                            }
                    );
                },
                WorkStop::notStarted
        );
    }

    public BreakStart startProjectBreak(Project project, UserContext user) {
        log.info("Starting break");
        return runningWorkAwareFunction(
                project,
                runningWork -> runningBreakAwareFunction(
                        project,
                        runningBreak -> BreakStart.breakAlreadyStarted(runningBreak.getId()),
                        () -> {
                            BreakStart started = BreakStart.started(timeRepository.startTime(project.getId(), TimeType.BREAK, user));
                            broadcastService.broadcast(user);
                            return started;
                        }
                ),
                BreakStart::workNotStarted
        );
    }

    public BreakStop endProjectBreak(Project project, UserContext user) {
        log.info("Stopping break");
        return runningBreakAwareFunction(
                project,
                runningBreak -> {
                    BreakStop stopped = BreakStop.stopped(stopTime(project, runningBreak, user).getId());
                    broadcastService.broadcast(user);
                    return stopped;
                },
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
                .selectBetween(
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
                    LocalTime breakTime = timeRepository.selectFromDate(project.getId(), runningWork.getStart())
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
        return timeRepository.select(project.getId(), time.getId()).get();
    }

    private <T> T runningBreakAwareFunction(Project project, Function<Time, T> whenRunning, Supplier<T> whenNotRunning) {
        return runningAwareFunction(TimeType.BREAK, project, whenRunning, whenNotRunning);
    }

    private <T> T runningWorkAwareFunction(Project project, Function<Time, T> whenRunning, Supplier<T> whenNotRunning) {
        return runningAwareFunction(TimeType.WORK, project, whenRunning, whenNotRunning);
    }

    private <T> T runningAwareFunction(TimeType type, Project project, Function<Time, T> whenRunning, Supplier<T> whenNotRunning) {
        Optional<Time> currentlyRunningOptional = timeRepository.selectWithNullEnd(project.getId(), type);
        if (currentlyRunningOptional.isPresent()) {
            return whenRunning.apply(currentlyRunningOptional.get());
        } else {
            return whenNotRunning.get();
        }
    }

}
