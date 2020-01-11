package com.altona.util;

import com.altona.context.TimeInfo;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ClockTimeInfo implements TimeInfo {

    @Override
    public Instant now() {
        return Instant.now();
    }

}
