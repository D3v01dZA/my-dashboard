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
import com.altona.dashboard.view.AbstractSecureView;
import com.altona.dashboard.view.UserInputDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TimeContent extends AbstractSecureView<ViewGroup> {

    private Spinner projectSpinner;

    private Button startButton;

    private LinearLayout secondaryButtonContainer;
    private Button stopButton;
    private Button pauseButton;

    public TimeContent(MainActivity mainActivity, LoginService loginService, Navigation navigation) {
        super(loginService, navigation, mainActivity.findViewById(R.id.time_content));
        this.projectSpinner = view.findViewById(R.id.time_project_spinner);
        this.startButton = view.findViewById(R.id.time_start_button);
        this.secondaryButtonContainer = view.findViewById(R.id.time_secondary_buttons);
        this.stopButton = view.findViewById(R.id.time_stop_button);
        this.pauseButton = view.findViewById(R.id.time_pause_button);
        setupButtons();
    }

    @Override
    public void onEnter() {
        loginService.tryExecute(new Request.Builder().get(), "/time/project", response -> {
            JsonArray elements = GsonHolder.INSTANCE.fromJson(response.getValue(), JsonArray.class);
            if (elements.size() == 0) {
                createProject();
            } else {
                ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(projectSpinner.getContext(), R.layout.support_simple_spinner_dropdown_item, new ArrayList<>());
                for (JsonElement element : elements) {
                    stringArrayAdapter.add(element.getAsJsonObject().get("name").getAsString());
                }
                projectSpinner.setAdapter(stringArrayAdapter);
            }
        }, error -> {
            Toast.makeText(view.getContext(), "Logging out because: " + error, Toast.LENGTH_SHORT).show();
            navigation.logout();
        });
    }

    private void createProject() {
        UserInputDialog.open(view.getContext(), "Create a Time Project", "",
                input -> loginService.tryExecute(
                        new Request.Builder().post(RequestBody.create(MediaType.get("application/json"), "{\"name\": \" " + input + " \"}")), "/time/project",
                        serviceResponse -> onEnter(),
                        failure -> navigation.enterMain()
                ),
                () -> navigation.enterMain());
    }

    private void setupButtons() {
        startButton.setOnClickListener(view -> start());
        stopButton.setOnClickListener(view -> stop());
        pauseButton.setOnClickListener(view -> pause());
    }

    private void start() {
        loginService.tryExecute(new Request.Builder().post(RequestBody.create(MediaType.get("application/json"), "")), "/time/project/1/start", response -> {}, failure -> {});
        startButton.setVisibility(View.GONE);
        secondaryButtonContainer.setVisibility(View.VISIBLE);
    }

    private void stop() {
        startButton.setVisibility(View.VISIBLE);
        secondaryButtonContainer.setVisibility(View.GONE);
        pauseButton.setText("Pause");
    }

    private void pause() {
        if ("Pause".equalsIgnoreCase(pauseButton.getText().toString())) {
            pauseButton.setText("Resume");
        } else {
            pauseButton.setText("Pause");
        }
    }

}
