package com.altona.service.synchronization.maconomy.model.create;

import com.altona.service.synchronization.maconomy.model.TimeData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {

    private TimeData data;

}
