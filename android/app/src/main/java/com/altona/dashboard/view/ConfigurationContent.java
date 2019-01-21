package com.altona.dashboard.view;

import android.view.View;
import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;

public class ConfigurationContent implements AppView {

    private ViewGroup configurationContent;

    public ConfigurationContent(MainActivity mainActivity) {
        this.configurationContent = mainActivity.findViewById(R.id.configuration_content);
    }

    @Override
    public void enter() {
        configurationContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void leave() {
        configurationContent.setVisibility(View.GONE);
    }

}
