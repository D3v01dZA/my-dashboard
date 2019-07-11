package com.altona.dashboard.view.configuration;

import androidx.annotation.NonNull;

import com.altona.dashboard.R;
import com.altona.dashboard.component.UsableRecycler;
import com.altona.dashboard.component.UsableRow;
import com.altona.dashboard.service.Settings;
import com.altona.dashboard.view.SecureAppActivity;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationActivity extends SecureAppActivity {

    public ConfigurationActivity() {
        super(R.layout.activity_configuration, true);
    }

    @Override
    protected void onCreate() {
        setupRecycler();
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void onHide() {

    }

    @Override
    protected void onShow() {

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    private UsableRecycler recycler() {
        return findViewById(R.id.recycler_view_settings);
    }

    private void setupRecycler() {
        Settings settings = new Settings(this);
        UsableRecycler recycler = recycler();
        List<UsableRow> usableRows = new ArrayList<>();
        usableRows.add(new StorageRow(this, settings));
        recycler.setup(usableRows);
    }

}
