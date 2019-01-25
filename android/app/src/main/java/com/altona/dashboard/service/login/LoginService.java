package com.altona.dashboard.service.login;

import android.util.Base64;

import com.altona.dashboard.view.BaseActivity;
import com.altona.dashboard.Static;
import com.altona.dashboard.service.ServiceResponse;
import com.altona.dashboard.service.Settings;
import com.altona.dashboard.view.ViewState;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class LoginService {

    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());

    private BaseActivity activity;

    private Settings settings;

    private ViewState viewState;

    public LoginService(BaseActivity activity, ViewState viewState) {
        this.activity = activity;
        this.viewState = viewState;
        this.settings = new Settings(activity);
    }

    public void tryExecute(Request.Builder builder, String subUrl, Consumer<ServiceResponse> onSuccess, Consumer<String> onFailure) {
        try {
            URL url = new URL(settings.getHost() + subUrl);
            String auth = Base64.encodeToString((credentials().getUsername() + ":" + credentials().getPassword()).getBytes(), Base64.DEFAULT);
            // Base64 returns an \n at the end which my Samsung REALLY doesn't like
            while (auth.endsWith("\n")) {
                auth = auth.substring(0, auth.length() - 1);
            }
            Static.HTTP_CLIENT.newCall(builder.url(url).header("Authorization", "Basic " + auth).build())
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            LOGGER.log(Level.SEVERE, "Failed to call " + subUrl, e);
                            activity.runOnUiThread(() -> onFailure.accept("Unknown IOException: " + e.getMessage()));
                        }

                        @Override
                        public void onResponse(Call call, Response response) {
                            try {
                                ServiceResponse serviceResponse = new ServiceResponse(response.code(), response.body().string());
                                activity.runOnUiThread(() -> onSuccess.accept(serviceResponse));
                            } catch (IOException e) {
                                LOGGER.log(Level.SEVERE, "Failed to call " + subUrl, e);
                                activity.runOnUiThread(() -> onFailure.accept("Unknown IOException: " + e.getMessage()));
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            activity.runOnUiThread(() -> onFailure.accept("Host " + settings.getHost() + " is not valid"));
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
            Static.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    activity.runOnUiThread(() -> onFailure.accept("Unknown IOException: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        String string = response.body().string();
                        if (string.startsWith("Root Controller")) {
                            if (remember) {
                                settings.setCredentials(credentials);
                            }
                            LoginService.this.viewState.setCredentials(credentials);
                            activity.runOnUiThread(() -> onSuccess.run());
                        } else {
                            settings.clearCredentials();
                            activity.runOnUiThread(() -> onFailure.accept("Wrong value received: " + string));
                        }
                    } else {
                        settings.clearCredentials();
                        activity.runOnUiThread(() -> onFailure.accept("Wrong response code received: " + response.code()));
                    }
                }
            });
        } catch (MalformedURLException e) {
            settings.clearCredentials();
            activity.runOnUiThread(() -> onFailure.accept("Host " + settings.getHost() + " is not valid"));
        }
    }

    public void logout() {
        viewState.setCredentials(null);
        settings.setCredentials(null);
    }

    public boolean isLoggedIn() {
        return credentials() != null;
    }

    public Optional<Credentials> getStoredCredentials() {
        if (viewState.getCredentials().isPresent()) {
            return viewState.getCredentials();
        }
        return settings.getCredentials();
    }

    private Credentials credentials() {
        return viewState.getCredentials().orElse(null);
    }

}
