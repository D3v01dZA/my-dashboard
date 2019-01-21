package com.altona.dashboard.view.settings;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.view.AppView;

public class SettingsContent implements AppView {

    private ViewGroup content;
    private RecyclerView recycler;

    private Settings settings;

    public SettingsContent(MainActivity mainActivity) {
        this.content = mainActivity.findViewById(R.id.settings_content);
        this.recycler = mainActivity.findViewById(R.id.recycler_view_settings);
        this.settings = new Settings(mainActivity);
        setupRecycler(mainActivity);
    }

    @Override
    public void enter() {
        content.setVisibility(View.VISIBLE);
    }

    @Override
    public void leave() {
        content.setVisibility(View.GONE);
    }

    private void setupRecycler(Context context) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recycler.setLayoutManager(layoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setAdapter(new SettingsRecyclerAdapter(context, settings));
        recycler.addOnItemTouchListener(new SettingsRecyclerTouchListener(context, recycler));
    }

}
