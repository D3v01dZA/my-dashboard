package com.altona.service.synchronization;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public interface Screenshotter extends TakesScreenshot, WebDriver {

    default void maximize() {
        manage().window().maximize();
    }

    default String takeScreenshot() {
        maximize();
        return getScreenshotAs(OutputType.BASE64);
    }

}
