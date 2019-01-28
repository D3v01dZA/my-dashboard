package com.altona.repository.integration.maconomy.create;

import com.altona.repository.integration.maconomy.TimeData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {

    private TimeData data;

}