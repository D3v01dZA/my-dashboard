package com.altona.util;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Driver {

    public static RemoteWebDriver getDriver() {
        if (Boolean.parseBoolean(System.getProperty("webdriver.chrome.docker"))) {
            return (RemoteWebDriver) RemoteWebDriver.builder()
                    .url(System.getProperty("webdriver.chrome.host"))
                    .setCapability("browserName", "chrome")
                    .build();
        }
        ChromeDriverService chromeDriverService = new ChromeDriverService.Builder()
                .withSilent(Boolean.parseBoolean(System.getProperty("webdriver.chrome.silent")))
                .build();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(Boolean.parseBoolean(System.getProperty("webdriver.chrome.headless")));
        if (Boolean.parseBoolean(System.getProperty("webdriver.chrome.linux"))) {
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--no-sandbox");
        }
        return new ChromeDriver(chromeDriverService, chromeOptions);
    }

}
