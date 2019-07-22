package com.altona.service.synchronization;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public interface Screenshotter extends TakesScreenshot, WebDriver {

    default Screenshot takeScreenshot() {
        manage().window().setSize(new Dimension(1920, 1080));
        return new Screenshot(getScreenshotAs(OutputType.BASE64));
    }

}
