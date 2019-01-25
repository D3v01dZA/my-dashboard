package com.altona.dashboard.view.settings;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.altona.dashboard.R;
import com.altona.dashboard.view.InsecureAppActivity;

public class SettingsActivity extends InsecureAppActivity {

    public SettingsActivity() {
        super(R.layout.activity_settings, true);
    }

    @Override
    public void onCreate() {
        setupRecycler();
    }

    @Override
    public void onEnter() {
        setupRecycler();
    }

    @Override
    protected void onLeave() {

    }

    @Override
    public void onHide() {

    }

    @Override
    protected void onShow() {

    }

    private RecyclerView recycler() {
        return findViewById(R.id.recycler_view_settings);
    }

    private void setupRecycler() {
        RecyclerView recycler = recycler();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setAdapter(new SettingsRecyclerAdapter(this));
        recycler.addOnItemTouchListener(new SettingsRecyclerTouchListener(this, recycler));
    }
}
