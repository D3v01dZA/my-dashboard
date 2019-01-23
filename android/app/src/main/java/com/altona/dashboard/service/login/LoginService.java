package com.altona.dashboard.service.login;

import android.util.Base64;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.service.ServiceResponse;
import com.altona.dashboard.view.settings.Credentials;
import com.altona.dashboard.view.settings.Settings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginService {

    private MainActivity mainActivity;

    private Settings settings;
    private OkHttpClient httpClient;

    private Credentials credentials;

    public LoginService(MainActivity mainActivity, Settings settings, OkHttpClient httpClient) {
        this.mainActivity = mainActivity;
        this.settings = settings;
        this.httpClient = httpClient;
    }

    public void tryExecute(Request.Builder builder, String subUrl, Consumer<ServiceResponse> onSuccess, Consumer<String> onFailure) {
        try {
            URL url = new URL(settings.getHost() + subUrl);
            String auth = Base64.encodeToString((credentials.getUsername() + ":" + credentials.getPassword()).getBytes(), Base64.DEFAULT);
            // Base64 returns an \n at the end which my Samsung REALLY doesn't like
            while (auth.endsWith("\n")) {
                auth = auth.substring(0, auth.length() - 1);
            }
            httpClient.newCall(builder.url(url).header("Authorization", "Basic " + auth).build())
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mainActivity.runOnUiThread(() -> onFailure.accept("Unknown IOException: " + e.getMessage()));
                        }

                        @Override
                        public void onResponse(Call call, Response response) {
                            try {
                                ServiceResponse serviceResponse = new ServiceResponse(response.code(), response.body().string());
                                mainActivity.runOnUiThread(() -> onSuccess.accept(serviceResponse));
                            } catch (IOException e) {
                                mainActivity.runOnUiThread(() -> onFailure.accept("Unknown IOException: " + e.getMessage()));
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            mainActivity.runOnUiThread(() -> onFailure.accept("Host " + settings.getHost() + " is not valid"));
        }
    }

    public void tryLogin(boolean remember, Credentials credentials, Runnable onSuccess, Consumer<String> onFailure) {
        try {
            URL url = new URL(settings.getHost());
            String auth = Base64.encodeToString((credentials.getUsername() + ":" + credentials.getPassword()).getBytes(), Base64.DEFAULT);
            // Base64 returns an \n at the end which my Samsung REALLY doesn't like
            while (auth.endsWith("\n")) {
                auth = auth.substring(0, auth.length() - 1);
            }
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Authorization", "Basic " + auth)
                    .build();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainActivity.runOnUiThread(() -> onFailure.accept("Unknown IOException: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        String string = response.body().string();
                        if (string.startsWith("Root Controller")) {
                            if (remember) {
                                settings.setCredentials(credentials);
                            }
                            LoginService.this.credentials = credentials;
                            mainActivity.runOnUiThread(() -> onSuccess.run());
                        } else {
                            settings.clearCredentails();
                            mainActivity.runOnUiThread(() -> onFailure.accept("Wrong value received: " + string));
                        }
                    } else {
                        settings.clearCredentails();
                        mainActivity.runOnUiThread(() -> onFailure.accept("Wrong response code received: " + response.code()));
                    }
                }
            });
        } catch (MalformedURLException e) {
            settings.clearCredentails();
            mainActivity.runOnUiThread(() -> onFailure.accept("Host " + settings.getHost() + " is not valid"));
        }
    }

    public void logout() {
        settings.clearCredentails();
        credentials = null;
    }

    public boolean isLoggedIn() {
        return credentials != null;
    }

    public Optional<Credentials> getStoredCredentials() {
        return settings.getCredentials();
    }

}
