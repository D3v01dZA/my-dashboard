package com.altona.service.synchronization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class Screenshot {

    @Getter
    @NonNull
    private String base64;

    public static Screenshot take(Screenshotter screenshotter) {
        return new Screenshot(screenshotter.takeScreenshot());
    }

}
