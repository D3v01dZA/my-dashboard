package com.altona.service.synchronization.maconomy.model;

import com.altona.service.synchronization.Screenshotter;
import com.altona.util.Driver;
import lombok.experimental.Delegate;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class MaconomyContext implements WebDriver, TakesScreenshot, Screenshotter {

    @Delegate(types = { WebDriver.class, TakesScreenshot.class })
    private ChromeDriver webDriver = Driver.getChromeDriver();

}
