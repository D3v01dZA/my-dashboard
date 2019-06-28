package com.altona.html;

import com.altona.SpringTest;
import com.altona.service.broadcast.Broadcast;
import com.altona.service.broadcast.BroadcastDelete;
import com.altona.service.broadcast.BroadcastMessage;
import com.altona.service.broadcast.BroadcastUpdate;
import com.altona.service.broadcast.MockBroadcast;
import com.altona.service.project.model.Project;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationAttemptBroadcast;
import com.altona.service.synchronization.model.SynchronizationServiceType;
import com.altona.service.synchronization.model.SynchronizationStatus;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.service.time.model.control.BreakStart;
import com.altona.service.time.model.control.BreakStop;
import com.altona.service.time.model.control.TimeStatus;
import com.altona.service.time.model.control.WorkStart;
import com.altona.service.time.model.control.WorkStop;
import com.altona.service.time.util.TimeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IntegrationTests extends SpringTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TimeInfo timeInfo;

    @Test
    void basicTimeSequence() throws Exception {
        String root = mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        assertEquals("Root Controller test!", root);

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
        assertNoTimeStatus();
        BreakStart breakStartWorkNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-break"))
                        .andExpect(status().isOk()),
                BreakStart.class
        );
        assertEquals(BreakStart.Result.WORK_NOT_STARTED, breakStartWorkNotStarted.getResult());
        assertFalse(breakStartWorkNotStarted.getTimeId().isPresent());

        // Stop Break Without Work
        assertNoTimeStatus();
        BreakStop breakStopWorkNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-break"))
                        .andExpect(status().isOk()),
                BreakStop.class
        );
        assertEquals(BreakStop.Result.WORK_NOT_STARTED, breakStopWorkNotStarted.getResult());
        assertFalse(breakStopWorkNotStarted.getTimeId().isPresent());

        // Start Work Without Work
        assertNoTimeStatus();
        WorkStop workStopWorkNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-work"))
                        .andExpect(status().isOk()),
                WorkStop.class
        );
        assertEquals(WorkStop.Result.WORK_NOT_STARTED, workStopWorkNotStarted.getResult());
        assertFalse(workStopWorkNotStarted.getWorkTimeId().isPresent());
        assertFalse(workStopWorkNotStarted.getBreakTimeId().isPresent());

        // Start Work
        assertNoTimeStatus();
        WorkStart workStart = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-work"))
                        .andExpect(status().isOk()),
                WorkStart.class
        );
        assertEquals(WorkStart.Result.WORK_STARTED, workStart.getResult());
        MockBroadcast workStartBroadcast = getBroadcast();
        assertEquals("test", workStartBroadcast.getUser().getUsername());
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
        assertEquals("test", breakStartBroadcast.getUser().getUsername());
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
        assertEquals("test", breakStopBroadcast.getUser().getUsername());
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
        assertEquals("test", workStopBroadcast.getUser().getUsername());
        assertEquals(BroadcastMessage.Type.TIME, workStopBroadcast.getBroadcastMessage().getType());
        TimeStatus workStopTimeStatus = assertInstanceOf(workStopBroadcast.getBroadcastMessage().getMessage(), TimeStatus.class);
        assertEquals(TimeStatus.Status.NONE, workStopTimeStatus.getStatus());

        doReturn(instant(2019, 2, 2, 18, 0)).when(timeInfo).now();

        // Start Work
        assertNoTimeStatus();
        WorkStart workStartTwo = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-work"))
                        .andExpect(status().isOk()),
                WorkStart.class
        );
        assertEquals(WorkStart.Result.WORK_STARTED, workStartTwo.getResult());
        MockBroadcast workStartTwoBroadcast = getBroadcast();
        assertEquals("test", workStartTwoBroadcast.getUser().getUsername());
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
        assertEquals("test", breakStartTwoBroadcast.getUser().getUsername());
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
        assertEquals("test", workStopTwoBroadcast.getUser().getUsername());
        assertEquals(BroadcastMessage.Type.TIME, workStopTwoBroadcast.getBroadcastMessage().getType());
        TimeStatus workStopTwoTimeStatus = assertInstanceOf(workStopTwoBroadcast.getBroadcastMessage().getMessage(), TimeStatus.class);
        assertEquals(TimeStatus.Status.NONE, workStopTwoTimeStatus.getStatus());

        assertNoTimeStatus();
    }

    @Test
    void basicBroadcastSequence() throws Exception {
        // Get all broadcasts
        List<Broadcast> broadcasts = read(
                mvc.perform(get("/broadcast"))
                        .andExpect(status().isOk()),
                new TypeReference<List<Broadcast>>() {
                }
        );
        assertTrue(broadcasts.isEmpty());

        // Change broadcast
        Broadcast broadcast = read(
                mvc.perform(post("/broadcast/update", new BroadcastUpdate("B", "A")))
                        .andExpect(status().isOk()),
                Broadcast.class
        );
        assertEquals("A", broadcast.getBroadcast());

        // Change broadcast to same
        Broadcast broadcastDuplicated = read(
                mvc.perform(post("/broadcast/update", new BroadcastUpdate("B", "A")))
                        .andExpect(status().isOk()),
                Broadcast.class
        );
        assertEquals("A", broadcastDuplicated.getBroadcast());
        assertEquals(broadcast.getId(), broadcastDuplicated.getId());

        // Get all broadcasts
        List<Broadcast> broadcastsAfterCreate = read(
                mvc.perform(get("/broadcast"))
                        .andExpect(status().isOk()),
                new TypeReference<List<Broadcast>>() {
                }
        );
        assertEquals(1, broadcastsAfterCreate.size());
        assertTrue(broadcastsAfterCreate.stream().anyMatch(b -> b.getBroadcast().equals("A")));

        // Change broadcast, removing old
        Broadcast broadcastUpdate = read(
                mvc.perform(post("/broadcast/update", new BroadcastUpdate("A", "B")))
                        .andExpect(status().isOk()),
                Broadcast.class
        );
        assertEquals("B", broadcastUpdate.getBroadcast());

        //
        List<Broadcast> broadcastsAfterUpdate = read(
                mvc.perform(get("/broadcast"))
                        .andExpect(status().isOk()),
                new TypeReference<List<Broadcast>>() {
                }
        );
        assertEquals(1, broadcastsAfterUpdate.size());
        assertTrue(broadcastsAfterUpdate.stream().anyMatch(b -> b.getBroadcast().equals("B")));

        Broadcast broadcastDelete = read(
                mvc.perform(post("/broadcast/delete", new BroadcastDelete("B")))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                Broadcast.class
        );
        assertEquals("B", broadcastDelete.getBroadcast());
        assertEquals(broadcastUpdate.getId(), broadcastDelete.getId());

        mvc.perform(get("/broadcast/" + broadcastDelete.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void basicSynchronizeSequence() throws Exception {
        doReturn(instant(2019, 2, 2, 8, 0)).when(timeInfo).now();

        // Create a project
        Project project = read(
                mvc.perform(post("/time/project", new Project(11, "Test Project")))
                        .andExpect(status().isCreated()),
                Project.class
        );
        assertEquals("Test Project", project.getName());

        // Create a succeeding synchronizer
        Synchronization synchronization = read(
                mvc.perform(post("/time/project/" + project.getId() + "/synchronization", new Synchronization(12, SynchronizationServiceType.SUCCEEDING, objectMapper.createObjectNode())))
                        .andExpect(status().isCreated()),
                Synchronization.class
        );
        assertEquals(SynchronizationServiceType.SUCCEEDING, synchronization.getService());
        assertEquals(objectMapper.createObjectNode(), synchronization.getConfiguration());

        // Attempt to synchronize
        SynchronizationAttempt synchronizationAttempt = read(
                mvc.perform(post("/time/project/" + project.getId() + "/synchronization/" + synchronization.getId() + "/synchronize")),
                SynchronizationAttempt.class
        );
        assertEquals(Optional.empty(), synchronizationAttempt.getMessage());
        assertEquals(Optional.empty(), synchronizationAttempt.getScreenshot());
        assertEquals(SynchronizationStatus.PENDING, synchronizationAttempt.getStatus());
        assertEquals(synchronization.getId(), synchronizationAttempt.getSynchronizationId());

        // Hit the attempt endpoint to check it works, should be same as above
        SynchronizationAttempt synchronizationAttemptRead = read(
                mvc.perform(get("/time/project/" + project.getId() + "/synchronization/" + synchronization.getId() + "/attempt/" + synchronizationAttempt.getId()))
                        .andExpect(status().isOk()),
                SynchronizationAttempt.class
        );
        assertEquals(synchronizationAttempt.getId(), synchronizationAttemptRead.getId());
        assertEquals(Optional.empty(), synchronizationAttemptRead.getMessage());
        assertEquals(Optional.empty(), synchronizationAttemptRead.getScreenshot());
        assertEquals(SynchronizationStatus.PENDING, synchronizationAttemptRead.getStatus());
        assertEquals(synchronization.getId(), synchronizationAttemptRead.getSynchronizationId());

        // Fetch the broadcast after synchronizing
        MockBroadcast attemptBroadcast = getBroadcast();
        assertEquals("test", attemptBroadcast.getUser().getUsername());
        assertEquals(BroadcastMessage.Type.SYNCHRONIZE_ATTEMPT, attemptBroadcast.getBroadcastMessage().getType());
        SynchronizationAttemptBroadcast synchronizationAttemptBroadcast = assertInstanceOf(attemptBroadcast.getBroadcastMessage().getMessage(), SynchronizationAttemptBroadcast.class);
        assertEquals(SynchronizationStatus.SUCCESS, synchronizationAttemptBroadcast.getStatus());

        // Hit the attempt endpoint to check it works, should be same as above
        SynchronizationAttempt synchronizationAttemptSuccessRead = read(
                mvc.perform(get("/time/project/" + project.getId() + "/synchronization/" + synchronization.getId() + "/attempt/" + synchronizationAttempt.getId()))
                        .andExpect(status().isOk()),
                SynchronizationAttempt.class
        );
        assertEquals(synchronizationAttempt.getId(), synchronizationAttemptSuccessRead.getId());
        assertEquals(Optional.empty(), synchronizationAttemptSuccessRead.getMessage());
        assertTrue(synchronizationAttemptSuccessRead.getScreenshot().isPresent());
        assertEquals(SynchronizationStatus.SUCCESS, synchronizationAttemptSuccessRead.getStatus());
        assertEquals(synchronization.getId(), synchronizationAttemptSuccessRead.getSynchronizationId());

        // Hit the traces endpoint
        List<SynchronizationTrace> synchronizationTraces = read(
                mvc.perform(get("/time/project/" + project.getId() + "/synchronization/" + synchronization.getId() + "/attempt/" + synchronizationAttempt.getId() + "/trace"))
                        .andExpect(status().isOk()),
                new TypeReference<List<SynchronizationTrace>>() {
                }
        );
        assertEquals(1, synchronizationTraces.size());
        SynchronizationTrace synchronizationTrace = Iterables.getOnlyElement(synchronizationTraces);
        assertEquals("Load", synchronizationTrace.getStage());
        assertEquals(synchronizationAttempt.getId(), synchronizationTrace.getSynchronizationAttemptId());
        assertNotNull(synchronizationTrace.getScreenshot());
        assertNotNull(synchronizationTrace.getScreenshot().getBase64());

        // Create a failing synchronizer
        Synchronization failingSynchronization = read(
                mvc.perform(post("/time/project/" + project.getId() + "/synchronization", new Synchronization(12, SynchronizationServiceType.FAILING, objectMapper.createObjectNode())))
                        .andExpect(status().isCreated()),
                Synchronization.class
        );
        assertEquals(SynchronizationServiceType.FAILING, failingSynchronization.getService());
        assertEquals(objectMapper.createObjectNode(), failingSynchronization.getConfiguration());

        // Attempt to fail synchronize
        SynchronizationAttempt failingSynchronizationAttempt = read(
                mvc.perform(post("/time/project/" + project.getId() + "/synchronization/" + failingSynchronization.getId() + "/synchronize")),
                SynchronizationAttempt.class
        );
        assertEquals(Optional.empty(), failingSynchronizationAttempt.getMessage());
        assertEquals(Optional.empty(), failingSynchronizationAttempt.getScreenshot());
        assertEquals(SynchronizationStatus.PENDING, failingSynchronizationAttempt.getStatus());
        assertEquals(failingSynchronization.getId(), failingSynchronizationAttempt.getSynchronizationId());

        // Hit the attempt endpoint to check it works, should be same as above
        SynchronizationAttempt failingSynchronizationAttemptRead = read(
                mvc.perform(get("/time/project/" + project.getId() + "/synchronization/" + failingSynchronization.getId() + "/attempt/" + failingSynchronizationAttempt.getId()))
                        .andExpect(status().isOk()),
                SynchronizationAttempt.class
        );
        assertEquals(failingSynchronizationAttempt.getId(), failingSynchronizationAttemptRead.getId());
        assertEquals(Optional.empty(), failingSynchronizationAttemptRead.getMessage());
        assertEquals(Optional.empty(), failingSynchronizationAttemptRead.getScreenshot());
        assertEquals(SynchronizationStatus.PENDING, failingSynchronizationAttemptRead.getStatus());
        assertEquals(failingSynchronization.getId(), failingSynchronizationAttemptRead.getSynchronizationId());

        // Fetch the broadcast after failing synchronizing
        MockBroadcast failingAttemptBroadcast = getBroadcast();
        assertEquals("test", failingAttemptBroadcast.getUser().getUsername());
        assertEquals(BroadcastMessage.Type.SYNCHRONIZE_ATTEMPT, failingAttemptBroadcast.getBroadcastMessage().getType());
        SynchronizationAttemptBroadcast failingSynchronizationAttemptBroadcast = assertInstanceOf(failingAttemptBroadcast.getBroadcastMessage().getMessage(), SynchronizationAttemptBroadcast.class);
        assertEquals(SynchronizationStatus.FAILURE, failingSynchronizationAttemptBroadcast.getStatus());

        // Hit the attempt endpoint to check the failure
        SynchronizationAttempt synchronizationAttemptFailureRead = read(
                mvc.perform(get("/time/project/" + project.getId() + "/synchronization/" + failingSynchronization.getId() + "/attempt/" + failingSynchronizationAttemptBroadcast.getId()))
                        .andExpect(status().isOk()),
                SynchronizationAttempt.class
        );
        assertEquals(failingSynchronizationAttempt.getId(), synchronizationAttemptFailureRead.getId());
        assertEquals(Optional.of("This synchronizer always fails"), synchronizationAttemptFailureRead.getMessage());
        assertEquals(Optional.empty(), synchronizationAttemptFailureRead.getScreenshot());
        assertEquals(SynchronizationStatus.FAILURE, synchronizationAttemptFailureRead.getStatus());
        assertEquals(failingSynchronization.getId(), synchronizationAttemptFailureRead.getSynchronizationId());
    }

    private void assertNoTimeStatus() throws Exception {
        TimeStatusRepresentation timeStatus = read(
                mvc.perform(post("/time/project/time-status"))
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
                mvc.perform(post("/time/project/time-status"))
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