package com.altona.dashboard.view.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.altona.dashboard.TestSetting;
import com.altona.dashboard.nav.Navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Settings {

    private static final String HOST = "host";
    private static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    private SharedPreferences sharedPreferences;
    private Navigation navigation;
    private List<Entry> entries;

    public Settings(Context context, Navigation navigation) {
        this.sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.navigation = navigation;
        recreateEntries();
    }

    public String getHost() {
        return TestSetting.CURRENT.getHost(sharedPreferences.getString(HOST, "localhost:8080"));
    }

    private void setHost(String host) {
        sharedPreferences.edit()
                .putString(HOST, TestSetting.CURRENT.getHostToSave(host))
                .apply();
        navigation.logout();
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

    public void clearCredentails() {
        sharedPreferences.edit()
                .remove(USERNAME)
                .remove(PASSWORD)
                .apply();
    }

    List<Entry> getEntries() {
        return entries;
    }

    private void recreateEntries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry("Host", getHost(), this::setHost));
        this.entries = entries;
    }

    class Entry {

        private String key;
        private String value;
        private Consumer<String> setter;

        Entry(String key, String value, Consumer<String> setter) {
            this.key = key;
            this.value = value;
            this.setter = setter;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void set(String value) {
            setter.accept(value);
            recreateEntries();
        }
    }

}
