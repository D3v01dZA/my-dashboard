package com.altona.dashboard.view.time;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.altona.dashboard.GsonHolder;
import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.service.LoginService;
import com.altona.dashboard.service.ServiceResponse;
import com.altona.dashboard.view.AbstractSecureView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;

import okhttp3.Request;

public class TimeContent extends AbstractSecureView<ViewGroup> {

    private Spinner projectSpinner;

    private Button startButton;

    private LinearLayout secondaryButtonContainer;
    private Button stopButton;
    private Button pauseButton;

    public TimeContent(MainActivity mainActivity, LoginService loginService, Navigation navigation) {
        super(loginService, navigation, (ViewGroup) mainActivity.findViewById(R.id.time_content));
        this.projectSpinner = view.findViewById(R.id.time_project_spinner);
        this.startButton = view.findViewById(R.id.time_start_button);
        this.secondaryButtonContainer = view.findViewById(R.id.time_secondary_buttons);
        this.stopButton = view.findViewById(R.id.time_stop_button);
        this.pauseButton = view.findViewById(R.id.time_pause_button);
        setupButtons();
    }

    @Override
    public void onEnter() {
        loginService.tryExecute(new Request.Builder().url(loginService.getUrl("/time/project"))).consume(
                new ServiceResponse.SuccessConsumer() {
                    @Override
                    public void accept(int code, String value) {
                        JsonArray elements = GsonHolder.INSTANCE.fromJson(value, JsonArray.class);
                        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(projectSpinner.getContext(), R.layout.support_simple_spinner_dropdown_item, new ArrayList<String>());
                        for (JsonElement element : elements) {
                            stringArrayAdapter.add(element.getAsJsonObject().get("name").getAsString());
                        }
                        projectSpinner.setAdapter(stringArrayAdapter);
                    }
                },
                new ServiceResponse.FailureConsumer() {
                    @Override
                    public void accept(String message) {
                        Toast.makeText(view.getContext(), "Logging out because: " + message, Toast.LENGTH_SHORT).show();
                        navigation.logout();
                    }
                }
        );
    }

    private void setupButtons() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.GONE);
                secondaryButtonContainer.setVisibility(View.VISIBLE);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.VISIBLE);
                secondaryButtonContainer.setVisibility(View.GONE);
                pauseButton.setText("Pause");
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("Pause".equalsIgnoreCase(pauseButton.getText().toString())) {
                    pauseButton.setText("Resume");
                } else {
                    pauseButton.setText("Pause");
                }
            }
        });
    }

}
