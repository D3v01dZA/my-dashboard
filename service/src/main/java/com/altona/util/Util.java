package com.altona.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Util {

    public static void sleep(int millis) {
        try {
            log.info("Sleeping for {}", millis);
            Thread.sleep(millis);
            log.info("Finished sleeping for {}", millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleep() {
        sleep(3000);
    }

    private Util() {
        throw new IllegalStateException("Don't Construct Me");
    }

}
