package com.altona.service.synchronization.maconomy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Base64;

@AllArgsConstructor
public class MaconomyConfiguration {

    @Getter
    @NonNull
    private String url;

    @Getter
    @NonNull
    private String username;

    @Getter
    @NonNull
    private String password;

    @Getter
    @NonNull
    private String projectName;

    @Getter
    @NonNull
    private String taskName;

}
