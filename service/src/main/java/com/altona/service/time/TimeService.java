package com.altona.service.time;

import com.altona.broadcast.broadcaster.Broadcaster;
import com.altona.service.time.model.LocalizedTime;
import com.altona.service.time.model.summary.*;
import com.altona.user.service.User;
import com.altona.user.service.UserContext;
import com.altona.broadcast.broadcaster.BroadcastMessage;
import com.altona.service.project.model.Project;
import com.altona.service.time.model.Time;
import com.altona.service.time.model.TimeCombination;
import com.altona.service.time.model.TimeType;
import com.altona.service.time.model.control.BreakStart;
import com.altona.service.time.model.control.BreakStop;
import com.altona.service.time.model.control.TimeStatus;
import com.altona.service.time.model.control.WorkStart;
import com.altona.service.time.model.control.WorkStop;
import com.altona.service.time.util.SingleTimeStatusCollector;
import com.altona.service.time.util.TimeConfig;
import com.altona.service.time.util.TimeInfo;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class TimeService {

    private TimeRepository timeRepository;
    private Broadcaster broadcaster;

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
                                    broadcaster.broadcast(user, BroadcastMessage.timeStatus(timeStatus(project, user)));
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
                                broadcaster.broadcast(user, BroadcastMessage.timeStatus(timeStatus(project, user)));
                                return ended;
                            },
                            () -> {
                                WorkStop ended = WorkStop.ended(endedWork.getId());
                                broadcaster.broadcast(user, BroadcastMessage.timeStatus(timeStatus(project, user)));
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
                            broadcaster.broadcast(user, BroadcastMessage.timeStatus(timeStatus(project, user)));
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
                    broadcaster.broadcast(user, BroadcastMessage.timeStatus(timeStatus(project, user)));
                    return stopped;
                },
                () -> runningWorkAwareFunction(
                        project,
                        runningWork -> BreakStop.breakNotStarted(),
                        BreakStop::workNotStarted
                )
        );
    }

    public Optional<TimeStatus> timeStatus(List<Project> projects, TimeInfo timeInfo) {
        return timeStatusInternal(projects, timeInfo);
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

    public Optional<LocalizedTime> time(TimeConfig timeConfig, Project project, int timeId) {
        log.info("Retrieving time {}", timeId);
        return timeRepository
                .select(project.getId(), timeId)
                .map(time -> time.toLocalizedTime(timeConfig));
    }

    public Result<Optional<LocalizedTime>, String> replaceTime(TimeConfig timeConfig, Project project, int timeId, LocalizedTime localizedTime) {
        log.info("Replacing time {}", timeId);
        Optional<Time> existingTimeOptional = timeRepository.select(project.getId(), timeId);
        if (!existingTimeOptional.isPresent()) {
            return Result.success(Optional.empty());
        }
        Time existingTime = existingTimeOptional.get();
        Optional<LocalDateTime> replacedEnd = localizedTime.getEnd();
        if (replacedEnd.isPresent()) {
            if (!replacedEnd.get().isAfter(localizedTime.getStart())) {
                return Result.failure("End time cannot be before start time");
            }
            if (timeConfig.unlocalize(replacedEnd.get()).isAfter(timeConfig.now())) {
                return Result.failure("Cannot change time to the future");
            }
        }
        Time replacedTime = localizedTime.unlocalize(timeConfig);
        boolean isStartReplacement = !existingTime.getStart().equals(replacedTime.getStart());
        boolean isEndReplacement = !existingTime.getEnd().equals(replacedTime.getEnd());
        if (isStartReplacement && isEndReplacement) {
            return Result.failure("Cannot change both start and end simultaneously");
        }
        if (!existingTime.getEnd().isPresent() || !replacedTime.getEnd().isPresent()) {
            return Result.failure("Cannot end time through a replacement");
        }
        if (existingTime.getType() != replacedTime.getType()) {
            return Result.failure("Cannot change time type through a replacement");
        }
        if (replacedTime.getType() == TimeType.WORK) {
            if (
                    (isStartReplacement && existingTime.getStart().isBefore(replacedTime.getStart())) ||
                            (isEndReplacement && existingTime.getEnd().get().isAfter(replacedTime.getEnd().get()))
            ) {
                List<Time> toModify = timeRepository.selectBetween(project.getId(), existingTime.getStart(), existingTime.getEnd().get())
                        .stream()
                        .filter(time -> time.getId() != timeId)
                        .collect(Collectors.toList());
                if (toModify.stream().anyMatch(time -> time.getType() == TimeType.WORK)) {
                    return Result.failure("Should not have found any work time");
                }
                List<Time> toDelete = toModify.stream()
                        .filter(time -> (isStartReplacement && !time.getStart().isAfter(replacedTime.getStart())) || (isEndReplacement && !time.getEnd().get().isBefore(replacedTime.getEnd().get())))
                        .collect(Collectors.toList());
                toDelete.forEach(time -> timeRepository.deleteTime(project.getId(), time.getId()));
                timeRepository.updateTime(project.getId(), timeId, replacedTime.getStart(), replacedTime.getEnd().get());
                return Result.success(timeRepository.select(project.getId(), timeId).map(time -> time.toLocalizedTime(timeConfig)));
            } else if (
                    (isStartReplacement && existingTime.getStart().isAfter(replacedTime.getStart())) ||
                            (isEndReplacement && existingTime.getEnd().get().isBefore(replacedTime.getEnd().get()))
            ) {
                List<Time> existingTimes = timeRepository.selectBetween(project.getId(), replacedTime.getStart(), replacedTime.getEnd().get());
                long workCount = existingTimes.stream()
                        .filter(time -> time.getType() == TimeType.WORK)
                        .count();
                if (workCount > 1) {
                    return Result.failure("Cannot change time to overlap other work");
                }
                timeRepository.updateTime(project.getId(), timeId, replacedTime.getStart(), replacedTime.getEnd().get());
                return Result.success(timeRepository.select(project.getId(), timeId).map(time -> time.toLocalizedTime(timeConfig)));
            } else {
                return Result.failure("Work time change scenario not supported");
            }
        } else {
            return Result.failure("Cannot change Breaks");
        }
    }

    public Optional<LocalizedTime> deleteTime(TimeConfig timeConfig, Project project, int timeId) {
        log.info("Retrieving time {}", timeId);
        return timeRepository.select(project.getId(), timeId)
                .map(time -> {
                    if (time.getType() == TimeType.WORK) {
                        timeRepository.selectBetween(project.getId(), time.getStart(), time.getEnd().orElseGet(timeConfig::now))
                                .forEach(delete -> timeRepository.deleteTime(project.getId(), delete.getId()));
                    } else {
                        timeRepository.deleteTime(project.getId(), timeId);
                    }
                    return time.toLocalizedTime(timeConfig);
                });
    }

    public List<LocalizedTime> times(TimeConfig timeConfig, Project project, TimeRetrievalConfiguration configuration) {
        log.info("Retrieving times {}", configuration);
        return timeRepository
                .selectBetween(
                        project.getId(),
                        timeConfig.unlocalize(configuration.getFrom()),
                        timeConfig.unlocalize(configuration.getTo())
                ).stream()
                .map(time -> time.toLocalizedTime(timeConfig))
                .collect(Collectors.toList());
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
