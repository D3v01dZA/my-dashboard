package com.altona.dashboard.view.main;

import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.service.login.LoginService;
import com.altona.dashboard.view.NavigationStatus;
import com.altona.dashboard.view.SecureAppView;

public class MainContent extends SecureAppView<ViewGroup> {

    public MainContent(MainActivity mainActivity, LoginService loginService, Navigation navigation) {
        super(mainActivity, loginService, navigation, mainActivity.findViewById(R.id.main_content));
    }

    @Override
    public NavigationStatus onEnter() {
        return NavigationStatus.SUCCESS;
    }
}
