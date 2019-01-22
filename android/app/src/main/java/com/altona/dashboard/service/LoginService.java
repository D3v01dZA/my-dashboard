package com.altona.dashboard.service;

import com.altona.dashboard.view.settings.Settings;

public class LoginService {

    private Settings settings;

    private String username;
    private String password;

    public LoginService(Settings settings) {
        this.settings = settings;
    }

    public void setUsernameAndPassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean isLoggedIn() {
        return username != null;
    }

}
