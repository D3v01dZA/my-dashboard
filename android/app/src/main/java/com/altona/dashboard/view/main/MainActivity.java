package com.altona.dashboard.view.main;

import android.widget.Button;

import com.altona.dashboard.R;
import com.altona.dashboard.view.SecureAppActivity;
import com.altona.dashboard.view.time.TimeActivity;

public class MainActivity extends SecureAppActivity {

    public MainActivity() {
        super(R.layout.activity_main, true);
    }

    @Override
    protected void onCreate() {
        timeButton().setOnClickListener(button -> enter(TimeActivity.class));
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

    private Button timeButton() {
        return findViewById(R.id.time_button);
    }

}
