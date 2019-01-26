package com.altona.dashboard.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Optional;

public class Session {

    private SharedPreferences sharedPreferences;

    public Session(Context context) {
        this.sharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public Optional<String> getCookie() {
        return Optional.ofNullable(sharedPreferences.getString("cookie", null));
    }

    public void setCookie(String sessionCookie) {
        sharedPreferences.edit()
                .putString("cookie", sessionCookie)
                .apply();
    }

    public void clearCookie() {
        sharedPreferences.edit()
                .remove("cookie")
                .apply();
    }

}
