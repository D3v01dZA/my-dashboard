package com.altona.dashboard.view;

import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.service.LoginService;

public class ConfigurationContent extends AbstractSecureView<ViewGroup> {

    public ConfigurationContent(MainActivity mainActivity, LoginService loginService, Navigation navigation) {
        super(loginService, navigation, (ViewGroup) mainActivity.findViewById(R.id.configuration_content));
    }

    @Override
    public void onEnter() {

    }
}
