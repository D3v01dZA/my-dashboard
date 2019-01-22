package com.altona.dashboard.view;

import android.view.View;

import com.altona.dashboard.service.LoginService;

public class AbstractSecureView<T extends View> implements AppView {

    private LoginService loginService;
    protected T view;

    protected AbstractSecureView(LoginService loginService, T view) {
        this.loginService = loginService;
        this.view = view;
    }

    @Override
    public boolean enter(AppView loginRedirect) {
        if (loginService.isLoggedIn()) {
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
}
