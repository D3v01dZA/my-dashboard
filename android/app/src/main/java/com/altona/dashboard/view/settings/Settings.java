package com.altona.dashboard.view.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.altona.dashboard.MainActivity;

public class Settings {

    private static final String HOST = "host";

    private SharedPreferences sharedPreferences;

    Settings(MainActivity mainActivity) {
        this.sharedPreferences = mainActivity.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public String getHost() {
        return sharedPreferences.getString(HOST, "localhost:8080");
    }

    public void setHost(String host) {
        sharedPreferences.edit()
                .putString(HOST, host)
                .apply();
    }

}
