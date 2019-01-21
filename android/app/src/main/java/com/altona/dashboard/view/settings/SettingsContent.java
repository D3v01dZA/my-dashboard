package com.altona.dashboard.view.settings;

import android.view.View;
import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.view.AppView;

public class SettingsContent implements AppView {

    private ViewGroup settingsContent;
    private Settings settings;

    public SettingsContent(MainActivity mainActivity) {
        this.settingsContent = mainActivity.findViewById(R.id.settings_content);
        this.settings = new Settings(mainActivity);
    }

    @Override
    public void enter() {
        settingsContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void leave() {
        settingsContent.setVisibility(View.GONE);
    }

}
