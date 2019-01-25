package com.altona.dashboard.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.altona.dashboard.TestSetting;
import com.altona.dashboard.service.login.Credentials;

import java.util.Optional;

public class Settings {

    private static final String HOST = "host";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private SharedPreferences sharedPreferences;

    public Settings(Context context) {
        this.sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public String getHost() {
        return TestSetting.CURRENT.getHost(sharedPreferences.getString(HOST, "localhost:8080"));
    }

    public void setHost(String host) {
        sharedPreferences.edit()
                .putString(HOST, TestSetting.CURRENT.getHostToSave(host))
                .apply();
    }

    public Optional<Credentials> getCredentials() {
        if (sharedPreferences.contains(USERNAME)) {
            return Optional.of(new Credentials(sharedPreferences.getString(USERNAME, ""), sharedPreferences.getString(PASSWORD, "")));
        }
        return Optional.empty();
    }

    public void setCredentials(Credentials credentials) {
        sharedPreferences.edit()
                .putString(USERNAME, credentials.getUsername())
                .putString(PASSWORD, credentials.getPassword())
                .apply();
    }

    public void clearCredentials() {
        sharedPreferences.edit()
                .remove(USERNAME)
                .remove(PASSWORD)
                .apply();
    }

}
