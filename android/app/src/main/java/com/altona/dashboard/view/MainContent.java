package com.altona.dashboard.view;

import android.view.View;
import android.view.ViewGroup;

public class MainContent implements AppView {

    private ViewGroup mainContent;

    public MainContent(ViewGroup mainContent) {
        this.mainContent = mainContent;
    }

    @Override
    public void enter() {
        mainContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void leave() {
        mainContent.setVisibility(View.INVISIBLE);
    }

}
