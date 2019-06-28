package com.altona;

import liquibase.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import sun.awt.HeadlessToolkit;

import java.awt.*;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
public class Main {

    public static void main(String[] args) {
        String profiles = System.getProperty("spring.profiles.active");
        if (profiles == null) {
            System.setProperty("spring.profiles.active", getOsProfile());
        } else {
            System.setProperty("spring.profiles.active", profiles + "," + getOsProfile());
        }
        SpringApplication.run(Main.class, args);
    }

    public static String getOsProfile() {
        log.info("Operating system " + SystemUtils.OS_NAME);
        if (SystemUtils.IS_OS_MAC_OSX) {
            log.info("Decided on Mac OS X");
            return "mac";
        } else if (SystemUtils.IS_OS_WINDOWS) {
            log.info("Decided on Windows");
            return "windows";
        } else if (SystemUtils.IS_OS_LINUX) {
            if (Toolkit.getDefaultToolkit() instanceof HeadlessToolkit) {
                log.info("Decided on Linux Headless");
                return "linuxheadless";
            } else {
                log.info("Decided on Linux");
                return "linux";
            }
        } else {
            throw new RuntimeException("Unknown OS " + SystemUtils.OS_NAME);
        }
    }

}
