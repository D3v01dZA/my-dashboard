package com.altona.util;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

public class Driver {

    public static ChromeDriver getChromeDriver() {
        ChromeDriverService chromeDriverService = new ChromeDriverService.Builder()
                .withSilent(Boolean.valueOf(System.getProperty("webdriver.chrome.silent")))
                .build();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(Boolean.valueOf(System.getProperty("webdriver.chrome.headless")));
        if (Boolean.valueOf("webdriver.chrome.linux")) {
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--no-sandbox");
        }
        return new ChromeDriver(chromeDriverService, chromeOptions);
    }

}
