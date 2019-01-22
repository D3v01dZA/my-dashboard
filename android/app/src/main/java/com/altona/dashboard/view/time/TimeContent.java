package com.altona.dashboard.view.time;

import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.service.LoginService;
import com.altona.dashboard.view.AbstractSecureView;

public class TimeContent extends AbstractSecureView<ViewGroup> {

    public TimeContent(MainActivity mainActivity, LoginService loginService) {
        super(loginService, (ViewGroup) mainActivity.findViewById(R.id.time_content));
    }

}
