package com.altona.dashboard.view;

import com.altona.dashboard.view.login.LoginActivity;

public abstract class SecureAppActivity extends BaseActivity {

    protected SecureAppActivity(int activityId, boolean drawer) {
        super(activityId, drawer);
    }

    @Override
    protected final void create() {
        onCreate();
    }


    @Override
    public final void enter() {
        if (loginService().isLoggedIn()) {
            onEnter();
        } else {
            enter(LoginActivity.class, true);
        }
    }

    @Override
    public final void leave() {
        onLeave();
    }

    @Override
    protected final void show() {
        onShow();
    }

    @Override
    public void hide() {
        onHide();
    }

    protected abstract void onCreate();

    protected abstract void onEnter();

    protected abstract void onLeave();

    protected abstract void onHide();

    protected abstract void onShow();

}
