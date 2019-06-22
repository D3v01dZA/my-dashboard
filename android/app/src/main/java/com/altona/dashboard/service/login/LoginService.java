package com.altona.dashboard.service.login;

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import com.altona.dashboard.Static;
import com.altona.dashboard.service.ServiceResponse;
import com.altona.dashboard.service.Session;
import com.altona.dashboard.service.Settings;
import com.altona.dashboard.service.firebase.FirebaseUpdate;
import com.altona.dashboard.view.BaseActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.firebase.iid.FirebaseInstanceId;

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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginService implements CookieJar {

    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());

    private OkHttpClient httpClient;

    private Consumer<Runnable> foregroundExecutor;

    private Settings settings;
    private Session session;

    private LoginService(Context context, Consumer<Runnable> foregroundExecutor) {
        this.foregroundExecutor = foregroundExecutor;
        // Effectively make timeouts take forever until they are async
        this.httpClient = new OkHttpClient.Builder()
                .cookieJar(this)
                .readTimeout(5, TimeUnit.MINUTES)
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();
        this.settings = new Settings(context);
        this.session = new Session(context);
    }

    public LoginService(BaseActivity activity) {
        this(activity, activity::runOnUiThread);
    }

    public LoginService(Service service) {
        this(service, Runnable::run);
    }

    public LoginService(Context context) {
        this(context, Runnable::run);
    }

    // Execute stuff on the background thread first
    public <T> void tryExecute(
            Request.Builder builder,
            String subUrl,
            CheckedFunction<ServiceResponse, T> onSuccessBackgroundThread,
            Consumer<T> onSuccessUiThread,
            Consumer<String> onFailure,
            Runnable onUnauthenticated
    ) {
        if (settings.isDeleteFirebaseId()) {
            deleteFirebaseId();
        } else {
            Optional<String> unsavedFirebaseId = settings.getUnsavedFirebaseId();
            if (unsavedFirebaseId.isPresent()) {
                FirebaseUpdate firebaseUpdate = settings.getFirebaseId()
                        .map(oldId -> new FirebaseUpdate(oldId, unsavedFirebaseId.get()))
                        .orElseGet(() -> new FirebaseUpdate(null, unsavedFirebaseId.get()));
                updateFirebaseToken(firebaseUpdate);
            }
        }
        actuallyTryExecute(builder, subUrl, onSuccessBackgroundThread, onSuccessUiThread, onFailure, onUnauthenticated);
    }

    private <T> void actuallyTryExecute(
            Request.Builder builder,
            String subUrl,
            CheckedFunction<ServiceResponse, T> onSuccessBackgroundThread,
            Consumer<T> onSuccessUiThread,
            Consumer<String> onFailure,
            Runnable onUnauthenticated
    ) {
        try {
            URL url = new URL(settings.getHost() + subUrl);
            httpClient.newCall(builder.url(url).build())
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            LOGGER.log(Level.SEVERE, "Failed to call " + subUrl, e);
                            foregroundExecutor.accept(() -> onFailure.accept("Unknown IOException: " + e.getMessage()));
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
                                                failure -> {
                                                    settings.clearCredentials();
                                                    onFailure.accept(failure);
                                                },
                                                () -> {
                                                    settings.clearCredentials();
                                                    onUnauthenticated.run();
                                                }
                                        );
                                    } else {
                                        foregroundExecutor.accept(onUnauthenticated);
                                    }
                                } else if (code >= 500) {
                                    LOGGER.severe(() -> "Failed to call " + subUrl + " with " + code);
                                    foregroundExecutor.accept(() -> onFailure.accept("Server " + code));
                                } else if (code >= 400) {
                                    LOGGER.warning(() -> "Failed to call " + subUrl + " with " + code);
                                    foregroundExecutor.accept(() -> onFailure.accept("Request " + code));
                                } else {
                                    T result = onSuccessBackgroundThread.apply(serviceResponse);
                                    foregroundExecutor.accept(() -> onSuccessUiThread.accept(result));
                                }
                            } catch (IOException e) {
                                LOGGER.log(Level.SEVERE, "Failed to call " + subUrl, e);
                                foregroundExecutor.accept(() -> onFailure.accept("Unknown IOException: " + e.getMessage()));
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            foregroundExecutor.accept(() -> onFailure.accept("Host " + settings.getHost() + " is not valid"));
        }
    }

    // Executes onSuccess and onFailure directly on main thread
    public void tryLogin(
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
                    foregroundExecutor.accept(() -> onFailure.accept("Unknown IOException: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        String string = response.body().string();
                        if (string.startsWith("Root Controller")) {
                            settings.setCredentials(credentials);
                            foregroundExecutor.accept(onSuccess);
                        } else {
                            settings.clearCredentials();
                            foregroundExecutor.accept(() -> onFailure.accept("Wrong value received: " + string));
                        }
                    } else {
                        settings.clearCredentials();
                        foregroundExecutor.accept(() -> onFailure.accept("Wrong response code received: " + response.code()));
                    }
                }
            });
        } catch (MalformedURLException e) {
            foregroundExecutor.accept(() -> onFailure.accept("Host " + settings.getHost() + " is not valid"));
        }
    }

    public void updateFirebaseToken(FirebaseUpdate firebaseUpdate) {
        try {
            actuallyTryExecute(
                    new Request.Builder()
                            .post(RequestBody.create(
                                    MediaType.get("application/json"),
                                    Static.OBJECT_MAPPER.writeValueAsString(firebaseUpdate))),
                    "/broadcast/update",
                    serviceResponse -> Static.OBJECT_MAPPER.readValue(serviceResponse.getValue(), ObjectNode.class),
                    success -> {
                        settings.setFirebaseId(firebaseUpdate.getNewBroadcast());
                        settings.clearUnsavedFirebaseId();
                        LOGGER.info(() -> String.format("Saved Broadcast %s", success));
                    },
                    failure -> {
                        settings.setUnsavedFirebaseId(firebaseUpdate.getNewBroadcast());
                        LOGGER.warning(() -> String.format("Failed Broadcast Save %s", failure));
                    },
                    () -> {
                        settings.setUnsavedFirebaseId(firebaseUpdate.getNewBroadcast());
                        LOGGER.warning(() -> "Failed Broadcast Save Because Unauthenticated");
                    }
            );
        } catch (
                JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize", e);
            throw new IllegalStateException("Failed to serialize", e);
        }
    }

    public void logout() {
        deleteFirebaseId();
        session.clearCookie();
        settings.clearCredentials();
    }

    private void deleteFirebaseId() {
        AsyncTask.execute(() -> {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
                settings.setDeleteFirebaseId(false);
            } catch (IOException e) {
                settings.setDeleteFirebaseId(true);
            }
        });
    }

    public boolean isLoggedIn() {
        return settings.getCredentials().isPresent();
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
