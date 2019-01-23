package com.altona.dashboard.view.time;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.altona.dashboard.GsonHolder;
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
        disableInteraction();
        timeService.getProjects(projects -> {
            if (projects.size() == 0) {
                createProject();
            } else {
                projectSpinner.setAdapter(new TimeSpinnerAdapter(mainActivity, projects));
                updateStatus();
            }
        }, this::logoutErrorHandler);
        return NavigationStatus.SUCCESS;
    }

    private void updateStatus() {
        timeService.timeStatus(currentProject(), timeStatus -> {
            timeStatus.getStatus().setButtons(this);
            enableInteraction();
        }, this::logoutErrorHandler);
    }

    private void createProject() {
        UserInputDialog.open(view.getContext(), "Create a Time Project", "",
                input -> loginService.tryExecute(
                        new Request.Builder().post(RequestBody.create(MediaType.get("application/json"), GsonHolder.INSTANCE.toJson(new Project(-1, input)))), "/time/project",
                        serviceResponse -> onEnter(),
                        failure -> navigation.enterMain()
                ),
                () -> navigation.enterMain());
    }

    private Project currentProject() {
        return (Project) projectSpinner.getSelectedItem();
    }

    private void setupButtons() {
        startButton.setOnClickListener(view -> start());
        stopButton.setOnClickListener(view -> stop());
        pauseButton.setOnClickListener(view -> pause());
    }

    private void disableInteraction() {
        projectSpinner.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
    }

    private void enableInteraction() {
        projectSpinner.setEnabled(true);
        startButton.setEnabled(true);
        stopButton.setEnabled(true);
        pauseButton.setEnabled(true);
    }

    private void start() {
        disableInteraction();
        Project project = currentProject();
        timeService.startWork(
                project,
                jsonObject -> {
                    Status.WORK.setButtons(this);
                    enableInteraction();
                },
                this::logoutErrorHandler
        );
    }

    private void stop() {
        disableInteraction();
        Project project = currentProject();
        timeService.stopWork(
                project,
                jsonObject -> {
                    Status.NONE.setButtons(this);
                    enableInteraction();
                },
                this::logoutErrorHandler
        );
    }

    private void pause() {
        disableInteraction();
        Project project = currentProject();
        if ("Pause".equalsIgnoreCase(pauseButton.getText().toString())) {
            timeService.startBreak(
                    project,
                    jsonObject -> {
                        Status.BREAK.setButtons(this);
                        enableInteraction();
                    },
                    this::logoutErrorHandler
            );
        } else {
            timeService.stopBreak(
                    project,
                    jsonObject -> {
                        Status.WORK.setButtons(this);
                        enableInteraction();
                    },
                    this::logoutErrorHandler
            );
        }
    }

    public enum Status {

        WORK {
            @Override
            public void setButtons(TimeContent timeContent) {
                timeContent.startButton.setVisibility(View.GONE);
                timeContent.secondaryButtonContainer.setVisibility(View.VISIBLE);
                timeContent.pauseButton.setText("Pause");
            }
        },
        BREAK {
            @Override
            public void setButtons(TimeContent timeContent) {
                timeContent.startButton.setVisibility(View.GONE);
                timeContent.secondaryButtonContainer.setVisibility(View.VISIBLE);
                timeContent.pauseButton.setText("Resume");
            }
        },
        NONE {
            @Override
            public void setButtons(TimeContent timeContent) {
                timeContent.startButton.setVisibility(View.VISIBLE);
                timeContent.secondaryButtonContainer.setVisibility(View.GONE);
            }
        };

        public abstract void setButtons(TimeContent timeContent);

    }
}
