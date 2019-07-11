package com.altona.dashboard.component;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.altona.dashboard.R;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class ToggleRow implements UsableRow {

    private String text;

    @Override
    public int view() {
        return R.layout.toggle_row;
    }

    @Override
    public void render(View view) {
        view.<TextView>findViewById(R.id.text).setText(text);
        Switch toggle = view.findViewById(R.id.toggle);
        toggle.setChecked(isChecked());
        toggle.setOnClickListener(toggleView -> onToggleClick((Switch) toggleView));
    }

    @Override
    public void onLongClick(UsableChangeNotifier changeNotifier) {

    }

    @Override
    public void onClick(UsableChangeNotifier changeNotifier) {

    }

    public abstract boolean isChecked();

    public abstract void onToggleClick(Switch toggle);

}
