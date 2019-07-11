package com.altona.dashboard.component;

import android.view.View;
import android.widget.TextView;

import com.altona.dashboard.R;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public abstract class TextRow implements UsableRow {

    @NonNull
    private String title;

    @NonNull
    private String value;

    @Override
    public int view() {
        return R.layout.text_row;
    }

    @Override
    public void render(View view) {
        view.<TextView>findViewById(R.id.setting_name).setText(title);
        view.<TextView>findViewById(R.id.setting_value).setText(value);
    }


}
