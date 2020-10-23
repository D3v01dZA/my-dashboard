package com.altona.service.synchronization.test.model;

import com.altona.service.synchronization.Screenshotter;
import com.altona.util.Driver;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

@RequiredArgsConstructor
public class SucceedingContext implements WebDriver, TakesScreenshot, Screenshotter {

    @Delegate(types = { WebDriver.class, TakesScreenshot.class })
    private RemoteWebDriver webDriver = Driver.getDriver();

}
