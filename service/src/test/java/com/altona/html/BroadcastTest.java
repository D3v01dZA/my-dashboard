package com.altona.html;

import com.altona.SpringTest;
import com.altona.broadcast.service.view.BroadcastDeleteView;
import com.altona.broadcast.service.view.BroadcastUpdateView;
import com.altona.broadcast.service.view.BroadcastView;
import com.altona.broadcast.service.view.UnsavedBroadcastView;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BroadcastTest extends SpringTest {

    @Autowired
    private MockMvc mvc;

    @Override
    protected String getTestUsername() {
        return "broadcast";
    }

    @Test
    void basicBroadcastSequence() throws Exception {
        // Get all broadcasts
        List<BroadcastView> broadcastViews = read(
                mvc.perform(get("/broadcast"))
                        .andExpect(status().isOk()),
                new TypeReference<List<BroadcastView>>() {
                }
        );
        assertTrue(broadcastViews.isEmpty());

        // Change broadcast
        BroadcastView broadcastView = read(
                mvc.perform(post("/broadcast/update", new BroadcastUpdateView("B", "A")))
                        .andExpect(status().isOk()),
                BroadcastView.class
        );
        assertEquals("A", broadcastView.getBroadcast());

        // Change broadcast to same
        BroadcastView broadcastViewDuplicated = read(
                mvc.perform(post("/broadcast/update", new BroadcastUpdateView("B", "A")))
                        .andExpect(status().isOk()),
                BroadcastView.class
        );
        assertEquals("A", broadcastViewDuplicated.getBroadcast());
        assertEquals(broadcastView.getId(), broadcastViewDuplicated.getId());

        // Get all broadcasts
        List<BroadcastView> broadcastsAfterCreate = read(
                mvc.perform(get("/broadcast"))
                        .andExpect(status().isOk()),
                new TypeReference<List<BroadcastView>>() {
                }
        );
        assertEquals(1, broadcastsAfterCreate.size());
        assertTrue(broadcastsAfterCreate.stream().anyMatch(b -> b.getBroadcast().equals("A")));

        // Change broadcast, removing old
        BroadcastView broadcastViewUpdate = read(
                mvc.perform(post("/broadcast/update", new BroadcastUpdateView("A", "B")))
                        .andExpect(status().isOk()),
                BroadcastView.class
        );
        assertEquals("B", broadcastViewUpdate.getBroadcast());

        //
        List<BroadcastView> broadcastsAfterUpdate = read(
                mvc.perform(get("/broadcast"))
                        .andExpect(status().isOk()),
                new TypeReference<List<BroadcastView>>() {
                }
        );
        assertEquals(1, broadcastsAfterUpdate.size());
        assertTrue(broadcastsAfterUpdate.stream().anyMatch(b -> b.getBroadcast().equals("B")));

        UnsavedBroadcastView broadcastViewDelete = read(
                mvc.perform(post("/broadcast/delete", new BroadcastDeleteView("B")))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                UnsavedBroadcastView.class
        );
        assertEquals("B", broadcastViewDelete.getBroadcast());

        mvc.perform(get("/broadcast/" + broadcastViewUpdate.getId()))
                .andExpect(status().isNotFound());
    }

}
