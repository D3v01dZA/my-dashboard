package com.altona.dashboard;

import com.altona.dashboard.service.login.Credentials;

public enum TestSetting {

    TEST_EMULATOR {
        @Override
        public String getSavedUsername(String actual) {
            return "test";
        }

        @Override
        public String getSavedPassword(String actual) {
            return "password";
        }

        @Override
        public String getHost() {
            return "http://10.0.2.2:8080";
        }
    },
    TEST_PHONE {
        @Override
        public String getSavedUsername(String actual) {
            return "test";
        }

        @Override
        public String getSavedPassword(String actual) {
            return "password";
        }

        @Override
        public String getHost() {
            return "http://192.168.0.113:8080";
        }
    },
    PROD {
        @Override
        public String getSavedUsername(String actual) {
            return actual;
        }

        @Override
        public String getSavedPassword(String actual) {
            return actual;
        }

        @Override
        public String getHost() {
            return "https://caltona.net/dashboard";
        }
    },
    TEST {
        @Override
        public String getSavedUsername(String actual) {
            return actual;
        }

        @Override
        public String getSavedPassword(String actual) {
            return actual;
        }

        @Override
        public String getHost() {
            return "https://caltona.net:1336";
        }
    };

    public static final TestSetting CURRENT = TEST_EMULATOR;

    public abstract String getHost();

    public Credentials getCredentials(String username, String password) {
        return new Credentials(getSavedUsername(username), getSavedPassword(password));
    }

    protected abstract String getSavedPassword(String actual);

    protected abstract String getSavedUsername(String actual);

}
