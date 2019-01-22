package com.altona.dashboard.view;

import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.service.LoginService;

public class MainContent extends AbstractSecureView<ViewGroup> {

    public MainContent(MainActivity mainActivity, LoginService loginService) {
        super(loginService, (ViewGroup) mainActivity.findViewById(R.id.main_content));
    }

}
