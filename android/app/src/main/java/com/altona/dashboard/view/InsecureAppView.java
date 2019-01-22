package com.altona.dashboard.view;

import android.view.View;

import com.altona.dashboard.MainActivity;

public abstract class InsecureAppView<T extends View> extends AppView<T> {

    protected InsecureAppView(MainActivity mainActivity, T view) {
        super(mainActivity, view);
    }

    @Override
    public NavigationStatus enter() {
        NavigationStatus navigationStatus = onEnter();
        if (navigationStatus == NavigationStatus.SUCCESS) {
            view.setVisibility(View.VISIBLE);
        }
        return navigationStatus;
    }

    @Override
    public void leave() {
        view.setVisibility(View.GONE);
    }

    public abstract NavigationStatus onEnter();

}
