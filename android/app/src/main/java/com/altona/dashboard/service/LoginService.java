package com.altona.dashboard.service;

import android.os.AsyncTask;
import android.util.Base64;

import com.altona.dashboard.view.settings.Settings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginService {

    private Settings settings;
    private OkHttpClient httpClient;

    private String username;
    private String password;

    public LoginService(Settings settings, OkHttpClient httpClient) {
        this.settings = settings;
        this.httpClient = httpClient;
    }

    public String getUrl(String url) {
        return settings.getHost() + url;
    }

    public ServiceResponse tryExecute(Request.Builder builder) {
        try {
            return new Execute(httpClient, builder, username, password).execute().get();
        } catch (ExecutionException e) {
            return ServiceResponse.failure("Unknown ExecutionException: " + e.getMessage());
        } catch (InterruptedException e) {
            return ServiceResponse.failure("Unknown InterruptedException: " + e.getMessage());
        }
    }

    public Optional<String> tryLogin(String username, String password) {
        try {
            Optional<String> result = new LoginAttempt(settings, httpClient, username, password).execute().get();
            if (!result.isPresent()) {
                this.username = username;
                this.password = password;
            }
            return result;
        } catch (ExecutionException e) {
            return Optional.of("Unknown ExecutionException: " + e.getMessage());
        } catch (InterruptedException e) {
            return Optional.of("Unknown InterruptedException: " + e.getMessage());
        }
    }

    public void logout() {
        username = null;
        password = null;
    }

    public boolean isLoggedIn() {
        return username != null;
    }

    private static class Execute extends AsyncTask<Void, Void, ServiceResponse> {

        private OkHttpClient httpClient;

        private Request.Builder builder;

        private String username;
        private String password;

        Execute(OkHttpClient httpClient, Request.Builder builder, String username, String password) {
            this.httpClient = httpClient;
            this.builder = builder;
            this.username = username;
            this.password = password;
        }

        @Override
        protected ServiceResponse doInBackground(Void... voids) {
            try {
                String auth = Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT);
                // Base64 return a \n at the end which my Samsung REALLY doesn't like
                while (auth.endsWith("\n")) {
                    auth = auth.substring(0, auth.length() - 1);
                }
                Request request = builder
                        .header("Authorization", "Basic " + auth)
                        .build();
                Response response = httpClient.newCall(request).execute();
                return ServiceResponse.success(response.code(), response.body().string());
            } catch (IOException e) {
                return ServiceResponse.failure("Unknown IOException: " + e.getMessage());
            }
        }

    }

    private static class LoginAttempt extends AsyncTask<Void, Void, Optional<String>> {

        private Settings settings;
        private OkHttpClient httpClient;

        private String username;
        private String password;

        LoginAttempt(Settings settings, OkHttpClient httpClient, String username, String password) {
            this.settings = settings;
            this.httpClient = httpClient;
            this.username = username;
            this.password = password;
        }

        @Override
        protected Optional<String> doInBackground(Void... voids) {
            try {
                URL url = new URL(settings.getHost());
                String auth = Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT);
                // Base64 return a \n at the end which my Samsung REALLY doesn't like
                while (auth.endsWith("\n")) {
                    auth = auth.substring(0, auth.length() - 1);
                }
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .header("Authorization", "Basic " + auth)
                        .build();
                Response response = httpClient.newCall(request).execute();
                if (response.code() == 200) {
                    String string = response.body().string();
                    if (string.startsWith("Root Controller")) {
                        return Optional.empty();
                    } else {
                        return Optional.of("Wrong value received: " + string);
                    }
                } else {
                    return Optional.of("Wrong response code received: " + response.code());
                }
            } catch (MalformedURLException e) {
                return Optional.of("Host " + settings.getHost() + " is not valid");
            } catch (IOException e) {
                return Optional.of("Unknown IOException: " + e.getMessage());
            }
        }
    }

}
