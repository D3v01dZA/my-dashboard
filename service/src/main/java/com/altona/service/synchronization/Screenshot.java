package com.altona.service.synchronization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

@AllArgsConstructor
public class Screenshot {

    @Getter
    @NonNull
    private String base64;

    public static Screenshot take(TakesScreenshot takesScreenshot) {
        return new Screenshot(takesScreenshot.getScreenshotAs(OutputType.BASE64));
    }

}
