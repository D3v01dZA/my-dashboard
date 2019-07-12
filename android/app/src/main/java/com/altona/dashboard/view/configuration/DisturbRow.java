package com.altona.dashboard.view.configuration;

import android.content.Intent;
import android.widget.Switch;

import com.altona.dashboard.component.ToggleRow;
import com.altona.dashboard.service.Settings;

public class DisturbRow extends ToggleRow {

    private ConfigurationActivity configurationActivity;
    private Settings settings;

    public DisturbRow(ConfigurationActivity configurationActivity, Settings settings) {
        super("Do Not Disturb");
        this.configurationActivity = configurationActivity;
        this.settings = settings;
    }

    @Override
    public boolean isChecked() {
        return settings.isDoNotDisturb();
    }

    @Override
    public void onToggleClick(Switch toggle) {
        if (toggle.isChecked()) {
            settings.setDoNotDisturb(true);
            if (!settings.haveDoNotDisturbPermission()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                configurationActivity.startActivity(intent);
            }
        } else {
            settings.setDoNotDisturb(false);
        }
    }
}
