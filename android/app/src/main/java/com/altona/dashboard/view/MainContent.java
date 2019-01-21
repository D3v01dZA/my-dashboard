package com.altona.dashboard.view;

import android.view.View;
import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;

public class MainContent implements AppView {

    private ViewGroup mainContent;

    public MainContent(MainActivity mainActivity) {
        this.mainContent = mainActivity.findViewById(R.id.main_content);
    }

    @Override
    public void enter() {
        mainContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void leave() {
        mainContent.setVisibility(View.GONE);
    }

}
