package com.altona.dashboard.view;

import android.view.View;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.nav.Navigation;

public abstract class InsecureAppView<T extends View> extends AppView<T> {

    protected InsecureAppView(MainActivity mainActivity, Navigation navigation, T view) {
        super(mainActivity, navigation, view);
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

    @Override
    public void hide() {
        onHide();
    }

    public abstract NavigationStatus onEnter();

    public abstract void onHide();

}
