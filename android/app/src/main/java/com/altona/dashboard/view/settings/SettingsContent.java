package com.altona.dashboard.view.settings;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.view.InsecureAppView;
import com.altona.dashboard.view.NavigationStatus;

public class SettingsContent extends InsecureAppView<ViewGroup> {

    private RecyclerView recycler;

    private Settings settings;

    public SettingsContent(MainActivity mainActivity, Navigation navigation, Settings settings) {
        super(mainActivity, navigation, mainActivity.findViewById(R.id.settings_content));
        this.recycler = mainActivity.findViewById(R.id.recycler_view_settings);
        this.settings = settings;
        setupRecycler(mainActivity);
    }

    @Override
    public NavigationStatus onEnter() {
        return NavigationStatus.SUCCESS;
    }

    @Override
    public void onHide() {

    }

    private void setupRecycler(Context context) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recycler.setLayoutManager(layoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setAdapter(new SettingsRecyclerAdapter(context, settings));
        recycler.addOnItemTouchListener(new SettingsRecyclerTouchListener(context, recycler));
    }
}
