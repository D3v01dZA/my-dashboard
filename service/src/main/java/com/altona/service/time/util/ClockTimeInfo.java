package com.altona.service.time.util;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ClockTimeInfo implements TimeInfo {

    @Override
    public Instant now() {
        return Instant.now();
    }

}
