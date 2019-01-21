package com.altona.dashboard.view;

import android.view.View;
import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;

public class TimeContent implements AppView {

    private ViewGroup timeContent;

    public TimeContent(MainActivity mainActivity) {
        this.timeContent = mainActivity.findViewById(R.id.time_content);
    }

    @Override
    public void enter() {
        timeContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void leave() {
        timeContent.setVisibility(View.GONE);
    }

}
