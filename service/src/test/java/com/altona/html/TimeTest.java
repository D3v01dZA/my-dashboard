package com.altona.html;

import com.altona.SpringTest;
import com.altona.broadcast.broadcaster.BroadcastMessage;
import com.altona.service.project.model.Project;
import com.altona.service.time.model.control.BreakStart;
import com.altona.service.time.model.control.BreakStop;
import com.altona.service.time.model.control.TimeStatus;
import com.altona.service.time.model.control.WorkStart;
import com.altona.service.time.model.control.WorkStop;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TimeTest extends SpringTest {

    @Override
    protected String getTestUsername() {
        return "sequence";
    }

    @Test
    void basicTimeSequence() throws Exception {
        Project project = read(
                mvc.perform(post("/time/project", new Project(11, "Test Project")))
                        .andExpect(status().isCreated()),
                Project.class
        );

        assertEquals("Test Project", project.getName());

        List<Project> projects = read(mvc.perform(get("/time/project"))
                        .andExpect(status().isOk()),
                new TypeReference<List<Project>>() {
                }
        );

        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("Test Project")));


        doReturn(instant(2019, 2, 2, 8, 0)).when(timeInfo).now();

        // Start Break Without Work
        assertNoTimeStatus(project);
        BreakStart breakStartWorkNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-break"))
                        .andExpect(status().isOk()),
                BreakStart.class
        );
        assertEquals(BreakStart.Result.WORK_NOT_STARTED, breakStartWorkNotStarted.getResult());
        assertFalse(breakStartWorkNotStarted.getTimeId().isPresent());

        // Stop Break Without Work
        assertNoTimeStatus(project);
        BreakStop breakStopWorkNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-break"))
                        .andExpect(status().isOk()),
                BreakStop.class
        );
        assertEquals(BreakStop.Result.WORK_NOT_STARTED, breakStopWorkNotStarted.getResult());
        assertFalse(breakStopWorkNotStarted.getTimeId().isPresent());

        // Start Work Without Work
        assertNoTimeStatus(project);
        WorkStop workStopWorkNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-work"))
                        .andExpect(status().isOk()),
                WorkStop.class
        );
        assertEquals(WorkStop.Result.WORK_NOT_STARTED, workStopWorkNotStarted.getResult());
        assertFalse(workStopWorkNotStarted.getWorkTimeId().isPresent());
        assertFalse(workStopWorkNotStarted.getBreakTimeId().isPresent());

        // Start Work
        assertNoTimeStatus(project);
        WorkStart workStart = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-work"))
                        .andExpect(status().isOk()),
                WorkStart.class
        );
        assertEquals(WorkStart.Result.WORK_STARTED, workStart.getResult());
        MockBroadcast workStartBroadcast = getBroadcast();
        assertEquals(getTestUserId(), workStartBroadcast.getContext().getUserId());
        assertEquals(BroadcastMessage.Type.TIME, workStartBroadcast.getBroadcastMessage().getType());
        TimeStatus workStartTimeStatus = assertInstanceOf(workStartBroadcast.getBroadcastMessage().getMessage(), TimeStatus.class);
        assertEquals(TimeStatus.Status.WORK, workStartTimeStatus.getStatus());

        doReturn(instant(2019, 2, 2, 12, 0)).when(timeInfo).now();

        // Start Work With Work Already Started
        assertWorkingTimeStatus(project, 4, 0);
        WorkStart workStartWorkAlreadyStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-work"))
                        .andExpect(status().isOk()),
                WorkStart.class
        );
        assertEquals(WorkStart.Result.WORK_ALREADY_STARTED, workStartWorkAlreadyStarted.getResult());
        assertEquals(workStart.getTimeId(), workStartWorkAlreadyStarted.getTimeId());

        // Stop Break With Work Already Started Without Break Start
        assertWorkingTimeStatus(project, 4, 0);
        BreakStop breakStopWorkStartedBreakNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-break"))
                        .andExpect(status().isOk()),
                BreakStop.class
        );
        assertEquals(BreakStop.Result.BREAK_NOT_STARTED, breakStopWorkStartedBreakNotStarted.getResult());
        assertFalse(breakStopWorkStartedBreakNotStarted.getTimeId().isPresent());

        // Start Break With Work
        assertWorkingTimeStatus(project, 4, 0);
        BreakStart breakStart = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-break"))
                        .andExpect(status().isOk()),
                BreakStart.class
        );
        assertEquals(BreakStart.Result.BREAK_STARTED, breakStart.getResult());
        assertTrue(breakStart.getTimeId().isPresent());
        MockBroadcast breakStartBroadcast = getBroadcast();
        assertEquals(getTestUserId(), breakStartBroadcast.getContext().getUserId());
        assertEquals(BroadcastMessage.Type.TIME, breakStartBroadcast.getBroadcastMessage().getType());
        TimeStatus breakStartTimeStatus = assertInstanceOf(breakStartBroadcast.getBroadcastMessage().getMessage(), TimeStatus.class);
        assertEquals(TimeStatus.Status.BREAK, breakStartTimeStatus.getStatus());

        doReturn(instant(2019, 2, 2, 13, 0)).when(timeInfo).now();

        // Start Break With Break Already Started
        assertBreakTimeStatus(project, 4, 1);
        BreakStart breakStartBreakAlreadyStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-break"))
                        .andExpect(status().isOk()),
                BreakStart.class
        );
        assertEquals(BreakStart.Result.BREAK_ALREADY_STARTED, breakStartBreakAlreadyStarted.getResult());
        assertEquals(breakStartBreakAlreadyStarted.getTimeId(), breakStart.getTimeId());

        // Stop Break With Break Started
        assertBreakTimeStatus(project, 4, 1);
        BreakStop breakStop = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-break"))
                        .andExpect(status().isOk()),
                BreakStop.class
        );
        assertEquals(BreakStop.Result.BREAK_STOPPED, breakStop.getResult());
        assertEquals(breakStop.getTimeId(), breakStart.getTimeId());
        MockBroadcast breakStopBroadcast = getBroadcast();
        assertEquals(getTestUserId(), breakStopBroadcast.getContext().getUserId());
        assertEquals(BroadcastMessage.Type.TIME, breakStopBroadcast.getBroadcastMessage().getType());
        TimeStatus breakStopTimeStatus = assertInstanceOf(breakStopBroadcast.getBroadcastMessage().getMessage(), TimeStatus.class);
        assertEquals(TimeStatus.Status.WORK, breakStopTimeStatus.getStatus());

        doReturn(instant(2019, 2, 2, 17, 0)).when(timeInfo).now();

        // Stop Work
        assertWorkingTimeStatus(project, 8, 1);
        WorkStop workStop = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-work"))
                        .andExpect(status().isOk()),
                WorkStop.class
        );
        assertEquals(WorkStop.Result.WORK_STOPPED, workStop.getResult());
        assertEquals(Optional.of(workStart.getTimeId()), workStop.getWorkTimeId());
        assertFalse(workStop.getBreakTimeId().isPresent());
        MockBroadcast workStopBroadcast = getBroadcast();
        assertEquals(getTestUserId(), workStopBroadcast.getContext().getUserId());
        assertEquals(BroadcastMessage.Type.TIME, workStopBroadcast.getBroadcastMessage().getType());
        TimeStatus workStopTimeStatus = assertInstanceOf(workStopBroadcast.getBroadcastMessage().getMessage(), TimeStatus.class);
        assertEquals(TimeStatus.Status.NONE, workStopTimeStatus.getStatus());

        doReturn(instant(2019, 2, 2, 18, 0)).when(timeInfo).now();

        // Start Work
        assertNoTimeStatus(project);
        WorkStart workStartTwo = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-work"))
                        .andExpect(status().isOk()),
                WorkStart.class
        );
        assertEquals(WorkStart.Result.WORK_STARTED, workStartTwo.getResult());
        MockBroadcast workStartTwoBroadcast = getBroadcast();
        assertEquals(getTestUserId(), workStartTwoBroadcast.getContext().getUserId());
        assertEquals(BroadcastMessage.Type.TIME, workStartTwoBroadcast.getBroadcastMessage().getType());
        TimeStatus workStartTwoTimeStatus = assertInstanceOf(workStartTwoBroadcast.getBroadcastMessage().getMessage(), TimeStatus.class);
        assertEquals(TimeStatus.Status.WORK, workStartTwoTimeStatus.getStatus());

        doReturn(instant(2019, 2, 2, 19, 0)).when(timeInfo).now();

        // Start Break
        assertWorkingTimeStatus(project, 1, 0);
        BreakStart breakStartTwo = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-break"))
                        .andExpect(status().isOk()),
                BreakStart.class
        );
        assertEquals(BreakStart.Result.BREAK_STARTED, breakStartTwo.getResult());
        assertTrue(breakStartTwo.getTimeId().isPresent());
        MockBroadcast breakStartTwoBroadcast = getBroadcast();
        assertEquals(getTestUserId(), breakStartTwoBroadcast.getContext().getUserId());
        assertEquals(BroadcastMessage.Type.TIME, breakStartTwoBroadcast.getBroadcastMessage().getType());
        TimeStatus breakStartTwoTimeStatus = assertInstanceOf(breakStartTwoBroadcast.getBroadcastMessage().getMessage(), TimeStatus.class);
        assertEquals(TimeStatus.Status.BREAK, breakStartTwoTimeStatus.getStatus());

        doReturn(instant(2019, 2, 2, 20, 0)).when(timeInfo).now();

        // Start Break
        assertBreakTimeStatus(project, 1, 1);
        WorkStop workStopTwo = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-work"))
                        .andExpect(status().isOk()),
                WorkStop.class
        );
        assertEquals(WorkStop.Result.WORK_AND_BREAK_STOPPED, workStopTwo.getResult());
        assertEquals(Optional.of(workStartTwo.getTimeId()), workStopTwo.getWorkTimeId());
        assertEquals(breakStartTwo.getTimeId(), workStopTwo.getBreakTimeId());
        MockBroadcast workStopTwoBroadcast = getBroadcast();
        assertEquals(getTestUserId(), workStopTwoBroadcast.getContext().getUserId());
        assertEquals(BroadcastMessage.Type.TIME, workStopTwoBroadcast.getBroadcastMessage().getType());
        TimeStatus workStopTwoTimeStatus = assertInstanceOf(workStopTwoBroadcast.getBroadcastMessage().getMessage(), TimeStatus.class);
        assertEquals(TimeStatus.Status.NONE, workStopTwoTimeStatus.getStatus());

        assertNoTimeStatus(project);
    }

    private void assertNoTimeStatus(Project project) throws Exception {
        TimeStatusRepresentation timeStatus = read(
                mvc.perform(post("/time/project/" + project.getId() + "/time-status"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                TimeStatusRepresentation.class
        );

        assertEquals(TimeStatus.Status.NONE, timeStatus.getStatus());
        assertFalse(timeStatus.getProjectId().isPresent());
        assertFalse(timeStatus.getTimeId().isPresent());
        assertFalse(timeStatus.getRunningWorkTotal().isPresent());
        assertFalse(timeStatus.getRunningBreakTotal().isPresent());
    }

    private void assertWorkingTimeStatus(Project project, int workHours, int breakHours) throws Exception {
        assertTimeStatus(project, TimeStatus.Status.WORK, workHours, breakHours);
    }

    private void assertBreakTimeStatus(Project project, int workHours, int breakHours) throws Exception {
        assertTimeStatus(project, TimeStatus.Status.BREAK, workHours, breakHours);
    }

    private void assertTimeStatus(Project project, TimeStatus.Status status, int workHours, int breakHours) throws Exception {
        TimeStatusRepresentation timeStatus = read(
                mvc.perform(post("/time/project/" + project.getId() + "/time-status"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                TimeStatusRepresentation.class
        );

        assertEquals(status, timeStatus.getStatus());
        assertEquals(Optional.of(project.getId()), timeStatus.getProjectId());
        assertTrue(timeStatus.getTimeId().isPresent());
        assertEquals(LocalTime.of(workHours, 0), timeStatus.getRunningWorkTotal().get());
        assertEquals(LocalTime.of(breakHours, 0), timeStatus.getRunningBreakTotal().get());
    }

    @AllArgsConstructor
    private static class TimeStatusRepresentation {

        @Getter
        private TimeStatus.Status status;
        private Integer projectId;
        private Integer timeId;
        private LocalTime runningWorkTotal;
        private LocalTime runningBreakTotal;

        public Optional<Integer> getProjectId() {
            return Optional.ofNullable(projectId);
        }

        public Optional<Integer> getTimeId() {
            return Optional.ofNullable(timeId);
        }

        public Optional<LocalTime> getRunningWorkTotal() {
            return Optional.ofNullable(runningWorkTotal);
        }

        public Optional<LocalTime> getRunningBreakTotal() {
            return Optional.ofNullable(runningBreakTotal);
        }

    }

}
