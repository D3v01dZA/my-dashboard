package com.altona.dashboard.view;

import android.view.View;

import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.service.LoginService;

public abstract class AbstractSecureView<T extends View> implements AppView {

    protected LoginService loginService;
    protected Navigation navigation;
    protected T view;

    protected AbstractSecureView(LoginService loginService, Navigation navigation, T view) {
        this.loginService = loginService;
        this.navigation = navigation;
        this.view = view;
    }

    @Override
    public boolean enter(AppView loginRedirect) {
        if (loginService.isLoggedIn()) {
            onEnter();
            view.setVisibility(View.VISIBLE);
            return true;
        } else {
            return loginRedirect.enter(null);
        }
    }

    @Override
    public void leave() {
        view.setVisibility(View.GONE);
    }

    public abstract void onEnter();

}
