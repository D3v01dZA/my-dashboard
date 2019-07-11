package com.altona.dashboard.view.configuration;

import android.Manifest;
import android.widget.Switch;

import androidx.core.app.ActivityCompat;

import com.altona.dashboard.component.ToggleRow;
import com.altona.dashboard.service.Settings;

public class StorageRow extends ToggleRow {

    private static final int STORAGE_REQUEST_CODE = 567;

    private ConfigurationActivity configurationActivity;
    private Settings settings;

    public StorageRow(ConfigurationActivity configurationActivity, Settings settings) {
        super("Store Photos");
        this.configurationActivity = configurationActivity;
        this.settings = settings;
    }

    @Override
    public boolean isChecked() {
        return settings.isSaveImages();
    }

    @Override
    public void onToggleClick(Switch toggle) {
        if (toggle.isChecked()) {
            settings.setSaveImages(true);
            if (!settings.haveWritePermission()) {
                ActivityCompat.requestPermissions(
                        configurationActivity,
                        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        STORAGE_REQUEST_CODE
                );
            }
        } else {
            settings.setSaveImages(false);
        }
    }
}
