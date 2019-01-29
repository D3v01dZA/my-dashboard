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

    @NonNull
    private String username;

    @NonNull
    private String password;

    public String getAuthorization() {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

}
