package com.altona.dashboard.view.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.nav.Navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Settings {

    private static final String HOST = "host";

    private SharedPreferences sharedPreferences;
    private Navigation navigation;
    private List<Entry> entries;

    public Settings(Context context, Navigation navigation) {
        this.sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.navigation = navigation;
        recreateEntries();
    }

    public String getHost() {
        if (MainActivity.TEST_BUILD) {
            return "http://192.168.1.80:8080";
        }
        return sharedPreferences.getString(HOST, "localhost:8080");
    }

    private void setHost(String host) {
        if (MainActivity.TEST_BUILD) {
            throw new IllegalStateException("Build is for test only");
        }
        sharedPreferences.edit()
                .putString(HOST, host)
                .apply();
        navigation.logout();
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
