package com.altona.dashboard.view;

import android.view.View;
import android.view.ViewGroup;

public class ConfigurationContent implements AppView {

    private ViewGroup configurationContent;

    public ConfigurationContent(ViewGroup configurationContent) {
        this.configurationContent = configurationContent;
    }

    @Override
    public void enter() {
        configurationContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void leave() {
        configurationContent.setVisibility(View.INVISIBLE);
    }

}
