package com.altona.dashboard.view;

import android.view.View;
import android.view.ViewGroup;

public class SettingsContent implements AppView {

    private ViewGroup settingsContent;

    public SettingsContent(ViewGroup settingsContent) {
        this.settingsContent = settingsContent;
    }

    @Override
    public void enter() {
        settingsContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void leave() {
        settingsContent.setVisibility(View.INVISIBLE);
    }

}
