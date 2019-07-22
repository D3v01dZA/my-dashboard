package com.altona.service.synchronization.openair.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class OpenairConfiguration {

    @NonNull
    private String companyId;

    @NonNull
    private String userId;

    @NonNull
    private String password;

    @NonNull
    private String project;

    @NonNull
    private String task;

}
