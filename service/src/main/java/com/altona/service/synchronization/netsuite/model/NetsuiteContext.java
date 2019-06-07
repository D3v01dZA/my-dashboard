package com.altona.service.synchronization.netsuite.model;

import com.altona.util.Driver;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

@RequiredArgsConstructor
public class NetsuiteContext implements WebDriver, TakesScreenshot {

    @Delegate(types = { WebDriver.class, TakesScreenshot.class })
    private ChromeDriver webDriver = Driver.getChromeDriver();

}
