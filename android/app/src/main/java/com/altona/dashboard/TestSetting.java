package com.altona.dashboard;

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
            return "http://192.168.1.80:8080";
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
            return "https://caltona.net:1337";
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

    public abstract String getSavedUsername(String actual);

    public abstract String getSavedPassword(String actual);

    public abstract String getHost();

}
