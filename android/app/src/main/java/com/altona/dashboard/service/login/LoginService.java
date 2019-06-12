package com.altona.dashboard.service.login;

import android.util.Base64;

import com.altona.dashboard.service.ServiceResponse;
import com.altona.dashboard.service.Session;
import com.altona.dashboard.service.Settings;
import com.altona.dashboard.view.BaseActivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginService implements CookieJar {

    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());

    private OkHttpClient httpClient;

    private BaseActivity activity;

    private Settings settings;
    private Session session;

    public LoginService(BaseActivity activity) {
        this.activity = activity;
        // Effectively make timeouts take forever until they are async
        this.httpClient = new OkHttpClient.Builder()
                .cookieJar(this)
                .readTimeout(5, TimeUnit.MINUTES)
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();
        this.settings = new Settings(activity);
        this.session = new Session(activity);
    }

    // Execute stuff on the background thread first
    public <T> void tryExecute(
            Request.Builder builder,
            String subUrl,
            CheckedFunction<ServiceResponse, T> onSuccessBackgroundThread,
            Consumer<T> onSuccessUiThread,
            Consumer<String> onFailure
    ) {
        try {
            URL url = new URL(settings.getHost() + subUrl);
            httpClient.newCall(builder.url(url).build())
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
                                int code = serviceResponse.getCode();
                                if (code == 401) {
                                    // Retry once in case of a server restart / session timeout
                                    LOGGER.warning(() -> "Failed to call " + subUrl + " with " + code);
                                    Optional<Credentials> credentials = getStoredCredentials();
                                    if (credentials.isPresent()) {
                                        LOGGER.warning(() -> "Retrying to call " + subUrl);
                                        session.clearCookie();
                                        tryExecute(
                                                builder.header("Authorization", basicAuth(credentials.get())),
                                                subUrl,
                                                onSuccessBackgroundThread,
                                                onSuccessUiThread,
                                                onFailure
                                        );
                                    } else {
                                        activity.runOnUiThread(() -> onFailure.accept("Timed out " + code));
                                    }
                                } else if (code >= 500) {
                                    LOGGER.severe(() -> "Failed to call " + subUrl + " with " + code);
                                    activity.runOnUiThread(() -> onFailure.accept("Server " + code));
                                } else if (code >= 400) {
                                    LOGGER.warning(() -> "Failed to call " + subUrl + " with " + code);
                                    activity.runOnUiThread(() -> onFailure.accept("Request " + code));
                                } else {
                                    T result = onSuccessBackgroundThread.apply(serviceResponse);
                                    activity.runOnUiThread(() -> onSuccessUiThread.accept(result));
                                }
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

    // Executes onSuccess and onFailure directly on main thread
    public void tryLogin(
            boolean remember,
            Credentials credentials,
            Runnable onSuccess,
            Consumer<String> onFailure
    ) {
        try {
            URL url = new URL(settings.getHost());
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Authorization", basicAuth(credentials))
                    .build();
            httpClient.newCall(request).enqueue(new Callback() {
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
            activity.runOnUiThread(() -> onFailure.accept("Host " + settings.getHost() + " is not valid"));
        }
    }

    public void logout() {
        session.clearCookie();
        settings.clearCredentials();
    }

    public boolean isLoggedIn() {
        return session.getCookie().isPresent();
    }

    public Optional<Credentials> getStoredCredentials() {
        return settings.getCredentials();
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.name().equalsIgnoreCase("JSESSIONID")) {
                session.setCookie(cookie.toString());
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return session.getCookie()
                .map(session -> Collections.singletonList(Cookie.parse(url, session)))
                .orElseGet(Collections::emptyList);
    }

    private String basicAuth(Credentials credentials) {
        String auth = Base64.encodeToString((credentials.getUsername() + ":" + credentials.getPassword()).getBytes(), Base64.DEFAULT);
        // Base64 returns an \n at the end which my Samsung REALLY doesn't like
        while (auth.endsWith("\n")) {
            auth = auth.substring(0, auth.length() - 1);
        }
        return "Basic " + auth;
    }

    public interface CheckedFunction<T, R> {

        R apply(T t) throws IOException;

    }

}
