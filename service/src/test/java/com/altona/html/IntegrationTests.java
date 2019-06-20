package com.altona.html;

import com.altona.SpringTest;
import com.altona.service.broadcast.Broadcast;
import com.altona.service.broadcast.BroadcastUpdate;
import com.altona.service.broadcast.FirebaseInteractor;
import com.altona.service.project.model.Project;
import com.altona.service.time.model.control.BreakStart;
import com.altona.service.time.model.control.BreakStop;
import com.altona.service.time.model.control.TimeStatus;
import com.altona.service.time.model.control.WorkStart;
import com.altona.service.time.model.control.WorkStop;
import com.altona.service.time.util.TimeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IntegrationTests extends SpringTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimeInfo timeInfo;

    @MockBean
    private FirebaseInteractor firebaseInteractor;

    @Test
    void basicTimeSequence() throws Exception {
        String root = mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        assertEquals("Root Controller test!", root);

        Project project = read(
                mvc.perform(post("/time/project", new Project(11, "Test Project")))
                        .andExpect(status().isCreated())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                Project.class
        );

        assertEquals("Test Project", project.getName());

        List<Project> projects = read(mvc.perform(get("/time/project"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                new TypeReference<List<Project>>() {
                }
        );

        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("Test Project")));


        doReturn(instant(2019, 2, 2, 8, 0)).when(timeInfo).now();

        // Start Break Without Work
        assertNoTimeStatus();
        BreakStart breakStartWorkNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-break"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                BreakStart.class
        );
        assertEquals(BreakStart.Result.WORK_NOT_STARTED, breakStartWorkNotStarted.getResult());
        assertFalse(breakStartWorkNotStarted.getTimeId().isPresent());

        // Stop Break Without Work
        assertNoTimeStatus();
        BreakStop breakStopWorkNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-break"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                BreakStop.class
        );
        assertEquals(BreakStop.Result.WORK_NOT_STARTED, breakStopWorkNotStarted.getResult());
        assertFalse(breakStopWorkNotStarted.getTimeId().isPresent());

        // Start Work Without Work
        assertNoTimeStatus();
        WorkStop workStopWorkNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-work"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                WorkStop.class
        );
        assertEquals(WorkStop.Result.WORK_NOT_STARTED, workStopWorkNotStarted.getResult());
        assertFalse(workStopWorkNotStarted.getWorkTimeId().isPresent());
        assertFalse(workStopWorkNotStarted.getBreakTimeId().isPresent());

        // Start Work
        assertNoTimeStatus();
        WorkStart workStart = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-work"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                WorkStart.class
        );
        assertEquals(WorkStart.Result.WORK_STARTED, workStart.getResult());

        doReturn(instant(2019, 2, 2, 12, 0)).when(timeInfo).now();

        // Start Work With Work Already Started
        assertWorkingTimeStatus(project, 4, 0);
        WorkStart workStartWorkAlreadyStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-work"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                WorkStart.class
        );
        assertEquals(WorkStart.Result.WORK_ALREADY_STARTED, workStartWorkAlreadyStarted.getResult());
        assertEquals(workStart.getTimeId(), workStartWorkAlreadyStarted.getTimeId());
        verify(firebaseInteractor, times(1)).send(any(), anyList(), anyMap());

        // Stop Break With Work Already Started Without Break Start
        assertWorkingTimeStatus(project, 4, 0);
        BreakStop breakStopWorkStartedBreakNotStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-break"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                BreakStop.class
        );
        assertEquals(BreakStop.Result.BREAK_NOT_STARTED, breakStopWorkStartedBreakNotStarted.getResult());
        assertFalse(breakStopWorkStartedBreakNotStarted.getTimeId().isPresent());

        // Start Break With Work
        assertWorkingTimeStatus(project, 4, 0);
        BreakStart breakStart = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-break"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                BreakStart.class
        );
        assertEquals(BreakStart.Result.BREAK_STARTED, breakStart.getResult());
        assertTrue(breakStart.getTimeId().isPresent());

        doReturn(instant(2019, 2, 2, 13, 0)).when(timeInfo).now();

        // Start Break With Break Already Started
        assertBreakTimeStatus(project, 4, 1);
        BreakStart breakStartBreakAlreadyStarted = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-break"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                BreakStart.class
        );
        assertEquals(BreakStart.Result.BREAK_ALREADY_STARTED, breakStartBreakAlreadyStarted.getResult());
        assertEquals(breakStartBreakAlreadyStarted.getTimeId(), breakStart.getTimeId());

        // Stop Break With Break Started
        assertBreakTimeStatus(project, 4, 1);
        BreakStop breakStop = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-break"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                BreakStop.class
        );
        assertEquals(BreakStop.Result.BREAK_STOPPED, breakStop.getResult());
        assertEquals(breakStop.getTimeId(), breakStart.getTimeId());

        doReturn(instant(2019, 2, 2, 17, 0)).when(timeInfo).now();

        // Stop Work
        assertWorkingTimeStatus(project, 8, 1);
        WorkStop workStop = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-work"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                WorkStop.class
        );
        assertEquals(WorkStop.Result.WORK_STOPPED, workStop.getResult());
        assertEquals(Optional.of(workStart.getTimeId()), workStop.getWorkTimeId());
        assertFalse(workStop.getBreakTimeId().isPresent());

        doReturn(instant(2019, 2, 2, 18, 0)).when(timeInfo).now();

        // Start Work
        assertNoTimeStatus();
        WorkStart workStartTwo = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-work"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                WorkStart.class
        );
        assertEquals(WorkStart.Result.WORK_STARTED, workStartTwo.getResult());

        doReturn(instant(2019, 2, 2, 19, 0)).when(timeInfo).now();

        // Start Break
        assertWorkingTimeStatus(project, 1, 0);
        BreakStart breakStartTwo = read(
                mvc.perform(post("/time/project/" + project.getId() + "/start-break"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                BreakStart.class
        );
        assertEquals(BreakStart.Result.BREAK_STARTED, breakStartTwo.getResult());
        assertTrue(breakStartTwo.getTimeId().isPresent());

        doReturn(instant(2019, 2, 2, 20, 0)).when(timeInfo).now();

        // Start Break
        assertBreakTimeStatus(project, 1, 1);
        WorkStop workStopTwo = read(
                mvc.perform(post("/time/project/" + project.getId() + "/stop-work"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                WorkStop.class
        );
        assertEquals(WorkStop.Result.WORK_AND_BREAK_STOPPED, workStopTwo.getResult());
        assertEquals(Optional.of(workStartTwo.getTimeId()), workStopTwo.getWorkTimeId());
        assertEquals(breakStartTwo.getTimeId(), workStopTwo.getBreakTimeId());

        assertNoTimeStatus();

    }

    @Test
    void basicBroadcastSequence() throws Exception {
        List<Broadcast> broadcasts = read(
                mvc.perform(get("/broadcast"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                new TypeReference<List<Broadcast>>() {
                }
        );
        assertTrue(broadcasts.isEmpty());

        Broadcast broadcast = read(
                mvc.perform(post("/broadcast/update", new BroadcastUpdate("B", "A")))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                Broadcast.class
        );
        assertEquals("A", broadcast.getBroadcast());

        List<Broadcast> broadcastsAfterCreate = read(
                mvc.perform(get("/broadcast"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                new TypeReference<List<Broadcast>>() {
                }
        );
        assertEquals(1, broadcastsAfterCreate.size());
        assertTrue(broadcastsAfterCreate.stream().anyMatch(b -> b.getBroadcast().equals("A")));

        Broadcast broadcastUpdate = read(
                mvc.perform(post("/broadcast/update", new BroadcastUpdate("A", "B")))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                Broadcast.class
        );
        assertEquals("B", broadcastUpdate.getBroadcast());

        List<Broadcast> broadcastsAfterUpdate = read(
                mvc.perform(get("/broadcast"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                new TypeReference<List<Broadcast>>() {
                }
        );
        assertEquals(1, broadcastsAfterUpdate.size());
        assertTrue(broadcastsAfterUpdate.stream().anyMatch(b -> b.getBroadcast().equals("B")));
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