package com.altona.dashboard.view;

import android.view.View;
import android.view.ViewGroup;

public class TimeContent implements AppView {

    private ViewGroup timeContent;

    public TimeContent(ViewGroup timeContent) {
        this.timeContent = timeContent;
    }

    @Override
    public void enter() {
        timeContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void leave() {
        timeContent.setVisibility(View.INVISIBLE);
    }

}
