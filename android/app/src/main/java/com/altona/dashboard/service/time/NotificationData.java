package com.altona.dashboard.service.time;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationData {

    private int icon;
    private String title;
    private String subTitle;
    private LocalTime time;

}
