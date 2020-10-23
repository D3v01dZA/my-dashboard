package com.altona.util;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@Configuration
@AllArgsConstructor
public class SystemProperties {

    private Environment environment;

    @PostConstruct
    public void postConstruct() {
        System.setProperty("webdriver.chrome.driver", environment.getProperty("webdriver.chrome.driver", ""));
        System.setProperty("webdriver.chrome.headless", environment.getProperty("webdriver.chrome.headless", ""));
        System.setProperty("webdriver.chrome.silent", environment.getProperty("webdriver.chrome.silent", ""));
        System.setProperty("webdriver.chrome.linux", environment.getProperty("webdriver.chrome.linux", ""));
        System.setProperty("webdriver.chrome.docker", environment.getProperty("webdriver.chrome.docker", ""));
        System.setProperty("webdriver.chrome.host", environment.getProperty("webdriver.chrome.host", ""));
    }

}
