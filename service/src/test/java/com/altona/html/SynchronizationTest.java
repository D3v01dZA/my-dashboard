package com.altona.html;

import com.altona.SpringTest;
import com.altona.broadcast.broadcaster.BroadcastMessage;
import com.altona.service.project.model.Project;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationAttemptBroadcast;
import com.altona.service.synchronization.model.SynchronizationServiceType;
import com.altona.service.synchronization.model.SynchronizationStatus;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.service.synchronization.test.model.SucceedingConfiguration;
import com.altona.service.time.util.TimeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SynchronizationTest extends SpringTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TimeInfo timeInfo;

    @Override
    protected String getTestUsername() {
        return "integration";
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
        SucceedingConfiguration succeedingConfiguration = new SucceedingConfiguration("https://www.google.com");
        Synchronization synchronization = read(
                mvc.perform(post("/time/project/" + project.getId() + "/synchronization", new Synchronization(12, true, SynchronizationServiceType.SUCCEEDING, objectMapper.valueToTree(succeedingConfiguration))))
                        .andExpect(status().isCreated()),
                Synchronization.class
        );
        assertEquals(SynchronizationServiceType.SUCCEEDING, synchronization.getService());
        assertEquals(objectMapper.valueToTree(succeedingConfiguration), synchronization.getConfiguration());

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
                mvc.perform(post("/time/project/" + project.getId() + "/synchronization", new Synchronization(12, true, SynchronizationServiceType.FAILING, objectMapper.createObjectNode())))
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

}