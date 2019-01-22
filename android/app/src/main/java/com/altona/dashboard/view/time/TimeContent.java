package com.altona.dashboard.view.time;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.service.login.LoginService;
import com.altona.dashboard.service.time.Project;
import com.altona.dashboard.service.time.TimeService;
import com.altona.dashboard.view.NavigationStatus;
import com.altona.dashboard.view.SecureAppView;
import com.altona.dashboard.view.util.UserInputDialog;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TimeContent extends SecureAppView<ViewGroup> {

    private TimeService timeService;

    private Spinner projectSpinner;

    private Button startButton;

    private LinearLayout secondaryButtonContainer;
    private Button stopButton;
    private Button pauseButton;

    public TimeContent(MainActivity mainActivity, LoginService loginService, Navigation navigation, TimeService timeService) {
        super(mainActivity, loginService, navigation, mainActivity.findViewById(R.id.time_content));
        this.timeService = timeService;
        this.projectSpinner = view.findViewById(R.id.time_project_spinner);
        this.startButton = view.findViewById(R.id.time_start_button);
        this.secondaryButtonContainer = view.findViewById(R.id.time_secondary_buttons);
        this.stopButton = view.findViewById(R.id.time_stop_button);
        this.pauseButton = view.findViewById(R.id.time_pause_button);
        setupButtons();
    }

    @Override
    public NavigationStatus onEnter() {
        timeService.getProjects(projects -> {
            if (projects.size() == 0) {
                createProject();
            } else {
                projectSpinner.setAdapter(new TimeSpinnerAdapter(mainActivity, projects));
            }
        }, this::logoutErrorHandler);
        return NavigationStatus.SUCCESS;
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
        Project project = (Project) projectSpinner.getSelectedItem();
        timeService.startWork(
                project,
                jsonObject -> {
                    startButton.setVisibility(View.GONE);
                    secondaryButtonContainer.setVisibility(View.VISIBLE);
                },
                this::logoutErrorHandler
        );
    }

    private void stop() {
        Project project = (Project) projectSpinner.getSelectedItem();
        timeService.stopWork(
                project,
                jsonObject -> {
                    startButton.setVisibility(View.VISIBLE);
                    secondaryButtonContainer.setVisibility(View.GONE);
                    pauseButton.setText("Pause");
                },
                this::logoutErrorHandler
        );
    }

    private void pause() {
        Project project = (Project) projectSpinner.getSelectedItem();
        if ("Pause".equalsIgnoreCase(pauseButton.getText().toString())) {
            timeService.startBreak(
                    project,
                    jsonObject -> pauseButton.setText("Resume"),
                    this::logoutErrorHandler
            );
        } else {
            timeService.stopBreak(
                    project,
                    jsonObject -> pauseButton.setText("Pause"),
                    this::logoutErrorHandler
            );
            ;
        }
    }

}
