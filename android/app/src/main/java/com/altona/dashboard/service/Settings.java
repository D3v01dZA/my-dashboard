package com.altona.dashboard.service;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.altona.dashboard.TestSetting;
import com.altona.dashboard.service.login.Credentials;

import java.util.Optional;

public class Settings {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String FIREBASE_ID = "firebase_id";
    private static final String UNSAVED_FIREBASE_ID = "firebase_unsaved_id";
    private static final String DELETE_FIREBASE_ID = "delete_firebase_id";
    private static final String SAVE_IMAGES = "save_images";

    private Context context;
    private SharedPreferences sharedPreferences;

    public Settings(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public String getHost() {
        return TestSetting.CURRENT.getHost();
    }

    public Optional<String> getCookie() {
        return Optional.ofNullable(sharedPreferences.getString("cookie", null));
    }

    public void setCookie(String sessionCookie) {
        sharedPreferences.edit()
                .putString("cookie", sessionCookie)
                .apply();
    }

    public void clearCookie() {
        sharedPreferences.edit()
                .remove("cookie")
                .apply();
    }

    public Optional<Credentials> getCredentials() {
        if (sharedPreferences.contains(USERNAME)) {
            return Optional.of(new Credentials(sharedPreferences.getString(USERNAME, ""), sharedPreferences.getString(PASSWORD, "")));
        }
        return Optional.empty();
    }

    public void setCredentials(Credentials credentials) {
        sharedPreferences.edit()
                .putString(USERNAME, credentials.getUsername())
                .putString(PASSWORD, credentials.getPassword())
                .apply();
    }

    public void clearCredentials() {
        sharedPreferences.edit()
                .remove(USERNAME)
                .remove(PASSWORD)
                .apply();
    }

    public Optional<String> getFirebaseId() {
        if (sharedPreferences.contains(FIREBASE_ID)) {
            return Optional.of(sharedPreferences.getString(FIREBASE_ID, ""));
        }
        return Optional.empty();
    }

    public void setFirebaseId(String firebaseId) {
        sharedPreferences.edit()
                .putString(FIREBASE_ID, firebaseId)
                .apply();
    }

    public Optional<String> getUnsavedFirebaseId() {
        if (sharedPreferences.contains(UNSAVED_FIREBASE_ID)) {
            return Optional.of(sharedPreferences.getString(UNSAVED_FIREBASE_ID, ""));
        }
        return Optional.empty();
    }

    public boolean isDeleteFirebaseId() {
        return sharedPreferences.getBoolean(DELETE_FIREBASE_ID, false);
    }

    public void setDeleteFirebaseId(boolean deleteFirebaseId) {
        sharedPreferences.edit()
                .putBoolean(DELETE_FIREBASE_ID, deleteFirebaseId)
                .apply();
    }

    public void setUnsavedFirebaseId(String firebaseId) {
        sharedPreferences.edit()
                .putString(UNSAVED_FIREBASE_ID, firebaseId)
                .apply();
    }

    public void clearUnsavedFirebaseId() {
        sharedPreferences.edit()
                .remove(UNSAVED_FIREBASE_ID)
                .apply();
    }

    public boolean haveWritePermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void setSaveImages(boolean image) {
        sharedPreferences.edit()
                .putBoolean(SAVE_IMAGES, image)
                .apply();
    }

    public boolean isSaveImages() {
        return preferenceIsSaveImages() && haveWritePermission();
    }

    private boolean preferenceIsSaveImages() {
        return sharedPreferences.getBoolean(SAVE_IMAGES, false);
    }

}
