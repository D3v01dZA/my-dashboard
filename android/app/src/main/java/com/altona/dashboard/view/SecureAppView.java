package com.altona.dashboard.view;

import android.view.View;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.service.login.LoginService;

public abstract class SecureAppView<T extends View> extends AppView<T> {

    protected LoginService loginService;

    protected SecureAppView(MainActivity mainActivity, LoginService loginService, Navigation navigation, T view) {
        super(mainActivity, navigation, view);
        this.loginService = loginService;
    }

    @Override
    public NavigationStatus enter() {
        if (loginService.isLoggedIn()) {
            NavigationStatus navigationStatus = onEnter();
            if (navigationStatus == NavigationStatus.SUCCESS) {
                view.setVisibility(View.VISIBLE);
            }
            return navigationStatus;
        } else {
            return NavigationStatus.LOGIN_REDIRECT;
        }
    }

    @Override
    public void leave() {
        view.setVisibility(View.GONE);
    }

    public abstract NavigationStatus onEnter();

}
