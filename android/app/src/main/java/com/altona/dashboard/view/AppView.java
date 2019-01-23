package com.altona.dashboard.view;

import android.view.View;
import android.widget.Toast;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.nav.Navigation;

public abstract class AppView<T extends View> {

    protected MainActivity mainActivity;
    protected T view;
    protected Navigation navigation;

    AppView(MainActivity mainActivity, Navigation navigation, T view) {
        this.mainActivity = mainActivity;
        this.navigation = navigation;
        this.view = view;
    }

    public abstract NavigationStatus enter();

    public abstract void leave();

    public abstract void hide();

    protected void toast(String message) {
        Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show();
    }

    protected void longToast(String message) {
        Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show();
    }

    protected void hideKeyboard() {
        mainActivity.hideKeyboard();
    }

    protected void logoutErrorHandler(String message) {
        longToast("Error: " + message);
        navigation.logout();
    }

}
