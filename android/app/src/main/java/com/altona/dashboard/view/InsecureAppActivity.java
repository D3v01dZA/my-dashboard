package com.altona.dashboard.view;

public abstract class InsecureAppActivity extends BaseActivity {

    protected InsecureAppActivity(int activityId, boolean drawer) {
        super(activityId, drawer);
    }

    @Override
    protected void create() {
        onCreate();
    }

    @Override
    public final void enter() {
        onEnter();
    }

    @Override
    public final void leave() {
        onLeave();
    }

    @Override
    public final void hide() {
        onHide();
    }

    @Override
    protected final void show() {
        onShow();
    }

    protected abstract void onCreate();

    protected abstract void onEnter();

    protected abstract void onLeave();

    protected abstract void onHide();

    protected abstract void onShow();

}
