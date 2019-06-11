package com.altona.service.time.util;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ClockTimeInfo implements TimeInfo {

    @Override
    public Date now() {
        return new Date();
    }

}
