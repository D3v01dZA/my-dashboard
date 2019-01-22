package com.altona.dashboard.view;

import android.view.View;
import android.widget.Toast;

import com.altona.dashboard.MainActivity;

public abstract class AppView<T extends View> {

    protected MainActivity mainActivity;
    protected T view;

    AppView(MainActivity mainActivity, T view) {
        this.mainActivity = mainActivity;
        this.view = view;
    }

    public abstract NavigationStatus enter();

    public abstract void leave();

    protected void toast(String message) {
        Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show();
    }

    protected void hideKeyboard() {
        mainActivity.hideKeyboard();
    }

}
