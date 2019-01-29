package com.altona.dashboard.view.settings;

import android.view.View;
import android.widget.TextView;

import com.altona.dashboard.R;
import com.altona.dashboard.component.UsableChangeNotifier;
import com.altona.dashboard.component.UsableRecycler;
import com.altona.dashboard.service.Settings;
import com.altona.dashboard.view.InsecureAppActivity;
import com.altona.dashboard.view.util.UserInputDialog;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends InsecureAppActivity {

    public SettingsActivity() {
        super(R.layout.activity_settings, false);
    }

    @Override
    public void onCreate() {

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

    private UsableRecycler<RecyclerSettingRow> recycler() {
        return findViewById(R.id.recycler_view_settings);
    }

    private void setupRecycler() {
        Settings settings = new Settings(this);
        UsableRecycler<RecyclerSettingRow> recycler = recycler();
        List<RecyclerSettingRow> recyclerSettingRows = new ArrayList<>();
        recyclerSettingRows.add(new RecyclerSettingRow("Host", settings.getHost(), settings::setHost));
        recyclerSettingRows.add(new RecyclerSettingRow("Host", settings.getHost(), settings::setHost));
        recyclerSettingRows.add(new RecyclerSettingRow("Host", settings.getHost(), settings::setHost));

        recycler.setup(
                R.layout.setting_row,
                recyclerSettingRows,
                this::renderRow,
                this::handleClick,
                (recyclerSettingRow, changeNotifier) -> {}
        );
    }

    private void renderRow(View view, RecyclerSettingRow recyclerSettingRow) {
        view.<TextView>findViewById(R.id.setting_name).setText(recyclerSettingRow.getTitle());
        view.<TextView>findViewById(R.id.setting_value).setText(recyclerSettingRow.getValue());
    }

    private void handleClick(RecyclerSettingRow recyclerSettingRow, UsableChangeNotifier changeNotifier) {
        UserInputDialog.open(this, "Set " + recyclerSettingRow.getTitle(), recyclerSettingRow.getValue(), string -> {
            recyclerSettingRow.getSetter().accept(string);
            recyclerSettingRow.setValue(string);
            changeNotifier.notifyChange();
        } , () -> {});
    }

}
