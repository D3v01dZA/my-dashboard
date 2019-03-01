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
        System.setProperty("webdriver.chrome.driver", environment.getRequiredProperty("webdriver.chrome.driver"));
        System.setProperty("webdriver.chrome.headless", environment.getRequiredProperty("webdriver.chrome.headless"));
        System.setProperty("webdriver.chrome.silent", environment.getRequiredProperty("webdriver.chrome.silent"));
    }

}
