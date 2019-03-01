package com.altona.service.synchronization.netsuite.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

@RequiredArgsConstructor
public class NetsuiteContext implements WebDriver, TakesScreenshot {

    @Delegate(types = { WebDriver.class, TakesScreenshot.class })
    private ChromeDriver webDriver = getChromeDriver();

    private static ChromeDriver getChromeDriver() {
        ChromeDriverService chromeDriverService = new ChromeDriverService.Builder()
                .withSilent(Boolean.valueOf(System.getProperty("webdriver.chrome.silent")))
                .build();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(Boolean.valueOf(System.getProperty("webdriver.chrome.headless")));
        if (Boolean.valueOf("webdriver.chrome.linux")) {
            chromeOptions.addArguments("--disable-dev-shm-usage");
        }
        return new ChromeDriver(chromeDriverService, chromeOptions);
    }

}
