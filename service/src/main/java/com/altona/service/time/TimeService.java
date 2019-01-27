package com.altona.service.time;

import com.altona.repository.time.project.Project;
import com.altona.repository.time.time.Time;
import com.altona.repository.time.time.TimeRepository;
import com.altona.service.time.control.*;
import com.altona.service.time.summary.Summary;
import com.altona.service.time.summary.SummaryConfiguration;
import com.altona.service.time.summary.SummaryCreator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TimeService {

    private TimeRepository timeRepository;

    public WorkStart startProjectWork(List<Project> projects, Project project) {
        return timeStatusInternal(projects)
                .map(timeStatus -> WorkStart.alreadyStarted(timeStatus.getProjectId().get(), timeStatus.getTimeId().get()))
                .orElseGet(
                        () -> runningWorkAwareFunction(
                                project,
                                runningWork -> WorkStart.alreadyStarted(project.getId(), runningWork.getId()),
                                () -> WorkStart.started(project.getId(), timeRepository.startTime(project.getId(), Time.Type.WORK))
                        )
                );
    }

    public WorkStop endProjectWork(Project project) {
        return runningWorkAwareFunction(
                project,
                runningWork -> {
                    Date now = new Date();
                    Time endedWork = stopTime(project, runningWork, now);
                    return runningBreakAwareFunction(
                            project,
                            (runningBreak) -> WorkStop.ended(endedWork.getId(), stopTime(project, runningBreak, now).getId()),
                            () -> WorkStop.ended(endedWork.getId())
                    );
                },
                WorkStop::notStarted
        );
    }

    public BreakStart startProjectBreak(Project project) {
        return runningWorkAwareFunction(
                project,
                runningWork -> runningBreakAwareFunction(
                        project,
                        runningBreak -> BreakStart.breakAlreadyStarted(runningBreak.getId()),
                        () -> BreakStart.started(timeRepository.startTime(project.getId(), Time.Type.BREAK))
                ),
                BreakStart::workNotStarted
        );
    }

    public BreakStop endProjectBreak(Project project) {
        return runningBreakAwareFunction(
                project,
                runningBreak -> BreakStop.stopped(stopTime(project, runningBreak).getId()),
                () -> runningWorkAwareFunction(
                        project,
                        runningWork -> BreakStop.breakNotStarted(),
                        BreakStop::workNotStarted
                )
        );
    }

    public TimeStatus timeStatus(List<Project> projects) {
        return timeStatusInternal(projects)
                .orElseGet(TimeStatus::none);
    }

    public List<ZoneTime> zoneTimes(TimeZoneMapper timeZoneMapper, Project project) {
        return timeRepository.timeList(project.getId())
                .stream()
                .map(time -> new ZoneTime(timeZoneMapper, time))
                .collect(Collectors.toList());
    }

    public Optional<ZoneTime> zoneTime(TimeZoneMapper timeZoneMapper, Project project, int timeId) {
        return timeRepository.time(project.getId(), timeId)
                .map(time -> new ZoneTime(timeZoneMapper, time));
    }

    public Summary summary(TimeZoneMapper timeZoneMapper, Project project, SummaryConfiguration configuration) {
        List<ZoneTime> zoneTimeList = timeRepository
                .timeListBetween(
                        project.getId(),
                        timeZoneMapper.mapLocalDate(configuration.getFrom()),
                        timeZoneMapper.mapLocalDate(configuration.getTo())
                )
                .stream()
                .map(time -> new ZoneTime(timeZoneMapper, time))
                .collect(Collectors.toList());
        return SummaryCreator.create(configuration, zoneTimeList);
    }

    private Optional<TimeStatus> timeStatusInternal(List<Project> projects) {
        return projects.stream()
                .map(this::timeStatus)
                .collect(SingleTimeStatusCollector.INSTANCE);
    }

    private TimeStatus timeStatus(Project project) {
        return runningWorkAwareFunction(
                project,
                runningWork -> {
                    Date now = new Date();
                    LocalTime breakTime = timeRepository.timesFromDate(project.getId(), runningWork.getStart())
                            .stream()
                            .map(time -> {
                                Assert.isTrue(time.getType() == Time.Type.BREAK, "All times after a non-ended work should be breaks");
                                return time.time(now);
                            })
                            .reduce(
                                    LocalTime.of(0, 0),
                                    (timeOne, timeTwo) -> timeOne.plus(timeTwo.toNanoOfDay(), ChronoUnit.NANOS)
                            );
                    LocalTime workTime = runningWork.time(now).minus(breakTime.toNanoOfDay(), ChronoUnit.NANOS);
                    return runningBreakAwareFunction(project,
                            runningBreak -> TimeStatus.onBreak(project, runningBreak, workTime, breakTime),
                            () -> TimeStatus.atWork(project, runningWork, workTime, breakTime)
                    );
                },
                TimeStatus::none
        );
    }

    private Time stopTime(Project project, Time time) {
        return stopTime(project, time, new Date());
    }

    private Time stopTime(Project project, Time time, Date date) {
        timeRepository.stopTime(project.getId(), time.getId(), date);
        return timeRepository.time(project.getId(), time.getId()).get();
    }

    private <T> T runningBreakAwareFunction(Project project, Function<Time, T> whenRunning, Supplier<T> whenNotRunning) {
        return runningAwareFunction(Time.Type.BREAK, project, whenRunning, whenNotRunning);
    }

    private <T> T runningWorkAwareFunction(Project project, Function<Time, T> whenRunning, Supplier<T> whenNotRunning) {
        return runningAwareFunction(Time.Type.WORK, project, whenRunning, whenNotRunning);
    }

    private <T> T runningAwareFunction(Time.Type type, Project project, Function<Time, T> whenRunning, Supplier<T> whenNotRunning) {
        Optional<Time> currentlyRunningOptional = timeRepository.timeWithNullEnd(project.getId(), type);
        if (currentlyRunningOptional.isPresent()) {
            return whenRunning.apply(currentlyRunningOptional.get());
        } else {
            return whenNotRunning.get();
        }
    }

}
