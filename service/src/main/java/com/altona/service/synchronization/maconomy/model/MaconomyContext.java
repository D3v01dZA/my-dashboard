package com.altona.service.synchronization.maconomy.model;

import com.altona.service.synchronization.Screenshotter;
import com.altona.util.Driver;
import lombok.experimental.Delegate;
import org.openqa.selenium.By;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MaconomyContext implements WebDriver, TakesScreenshot, Screenshotter {

    @Delegate(types = { WebDriver.class, TakesScreenshot.class })
    private RemoteWebDriver webDriver = Driver.getDriver();

    public WebElement waitForElement(By by) {
        return new WebDriverWait(this, 30)
                .until(driver -> driver.findElement(by));
    }

}
