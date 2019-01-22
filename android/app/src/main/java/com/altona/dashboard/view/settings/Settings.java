package com.altona.dashboard.view.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Settings {

    private static final String HOST = "host";

    private SharedPreferences sharedPreferences;
    private List<Entry> entries;

    public Settings(Context context) {
        this.sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        recreateEntries();
    }

    public String getHost() {
        return sharedPreferences.getString(HOST, "localhost:8080");
    }

    private void setHost(String host) {
        sharedPreferences.edit()
                .putString(HOST, host)
                .apply();
    }

    List<Entry> getEntries() {
        return entries;
    }

    private void recreateEntries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry("Host", getHost(), new Consumer<String>() {
            @Override
            public void accept(String s) {
                setHost(s);
            }
        }));
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
