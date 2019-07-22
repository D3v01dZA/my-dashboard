package com.altona.service.synchronization.openair.model;

import com.altona.service.synchronization.Screenshotter;
import com.altona.util.Driver;
import lombok.experimental.Delegate;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.stream.Collectors;

public class OpenairContext implements WebDriver, TakesScreenshot, Screenshotter {

    @Delegate(types = {WebDriver.class, TakesScreenshot.class})
    private ChromeDriver webDriver = Driver.getChromeDriver();

    public WebElement waitForElement(By by) {
        return new WebDriverWait(this, 30)
                .until(driver -> driver.findElement(by));
    }

    public WebElement waitForElement(By parent, By child, String text) {
        return new WebDriverWait(this, 30)
                .until(driver -> {
                    List<WebElement> elements = driver.findElements(parent).stream()
                            .flatMap(webElement -> webElement.findElements(child).stream())
                            .filter(webElement -> text.equals(webElement.getText()))
                            .collect(Collectors.toList());
                    if (elements.size() != 1) {
                        throw new NoSuchElementException("Expected 1 element, found " + elements.size());
                    }
                    return elements.get(0);
                });
    }

    public void waitForElementDisappear(By by) {

    }

    public List<WebElement> waitForElements(By parent, By child, String text, int size) {
        return new WebDriverWait(this, 30)
                .until(driver -> {
                    List<WebElement> elements = waitForElement(parent).findElements(child).stream()
                            .filter(webElement -> text.equals(webElement.getText()))
                            .collect(Collectors.toList());
                    if (elements.size() != size) {
                        throw new NoSuchElementException("Expected " + size + " element(s), found " + elements.size());
                    }
                    return elements;
                });
    }

}
