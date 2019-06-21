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
        public String getHost(String preferencesHost) {
            return "http://10.0.2.2:8080";
        }

        @Override
        public String getHostToSave(String preferencesHost) {
            throw new IllegalStateException("Build is for test only");
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
        public String getHost(String preferencesHost) {
            return "http://192.168.1.80:8080";
        }

        @Override
        public String getHostToSave(String preferencesHost) {
            throw new IllegalStateException("Build is for test only");
        }
    },
    PHONE {
        @Override
        public String getSavedUsername(String actual) {
            return actual;
        }

        @Override
        public String getSavedPassword(String actual) {
            return actual;
        }

        @Override
        public String getHost(String preferencesHost) {
            return preferencesHost;
        }

        @Override
        public String getHostToSave(String preferencesHost) {
            return preferencesHost;
        }
    };

    public static final TestSetting CURRENT = TEST_EMULATOR;

    public abstract String getSavedUsername(String actual);

    public abstract String getSavedPassword(String actual);

    public abstract String getHost(String preferencesHost);

    public abstract String getHostToSave(String preferencesHost);

}
