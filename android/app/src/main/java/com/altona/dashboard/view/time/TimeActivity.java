package com.altona.dashboard.view.time;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.altona.dashboard.R;
import com.altona.dashboard.service.time.Project;
import com.altona.dashboard.service.time.TimeService;
import com.altona.dashboard.service.time.TimeStatus;
import com.altona.dashboard.view.SecureAppActivity;
import com.altona.dashboard.view.util.UserInputDialog;

import java.util.Timer;
import java.util.TimerTask;

public class TimeActivity extends SecureAppActivity {

    private TimeStatus currentStatus;
    private Timer timer;

    public TimeActivity() {
        super(R.layout.activity_time, true);
    }

    @Override
    protected void onCreate() {
        setupButtons();
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void onLeave() {
        if (currentStatus != null) {
            currentStatus = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onHide() {
        onLeave();
    }

    @Override
    protected void onShow() {
        disableInteraction();
        timeService().getProjects(projects -> {
            if (projects.size() == 0) {
                createProject();
            } else {
                projectSpinner().setAdapter(new TimeSpinnerAdapter(this, projects));
                updateStatus();
            }
        }, this::logoutErrorHandler);
    }

    private void updateStatus() {
        timeService().timeStatus(currentProject(), timeStatus -> {
            setCurrentStatus(timeStatus);
            enableInteractionAndUpdate();
        }, this::logoutErrorHandler);
    }

    private void createProject() {
        UserInputDialog.open(this, "Create a Time Project", "",
                input -> timeService().createProject(
                        new Project(-1, input),
                        serviceResponse -> onEnter(),
                        this::logoutErrorHandler
                ),
                () -> {});
    }

    private TimeService timeService() {
        return new TimeService(loginService());
    }

    private Project currentProject() {
        return (Project) projectSpinner().getSelectedItem();
    }

    private void setCurrentStatus(TimeStatus timeStatus) {
        this.currentStatus = timeStatus;
        TimeNotification.notify(this, timeStatus);
        updateWithCurrentStatus();
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> updateWithCurrentStatus());
            }
        }, 1000, 1000);
    }

    private void updateWithCurrentStatus() {
        if (currentStatus != null) { // We might rerun this one extra time on leaving
            currentStatus.update(this);
        }
    }

    private void setupButtons() {
        startButton().setOnClickListener(view -> start());
        stopButton().setOnClickListener(view -> stop());
        pauseButton().setOnClickListener(view -> pause());
    }

    private void disableInteraction() {
        projectSpinner().setEnabled(false);
        startButton().setEnabled(false);
        stopButton().setEnabled(false);
        pauseButton().setEnabled(false);
    }

    private void enableInteractionAndUpdate() {
        updateWithCurrentStatus();
        projectSpinner().setEnabled(true);
        startButton().setEnabled(true);
        stopButton().setEnabled(true);
        pauseButton().setEnabled(true);
    }

    private void start() {
        disableInteraction();
        Project project = currentProject();
        timeService().startWork(
                project,
                jsonObject -> {
                    currentStatus.startWork(this);
                    enableInteractionAndUpdate();
                },
                this::logoutErrorHandler
        );
    }

    private void stop() {
        disableInteraction();
        Project project = currentProject();
        timeService().stopWork(
                project,
                jsonObject -> {
                    currentStatus.stopWork(this);
                    enableInteractionAndUpdate();
                },
                this::logoutErrorHandler
        );
    }

    private void pause() {
        disableInteraction();
        Project project = currentProject();
        if ("Pause".equalsIgnoreCase(pauseButton().getText().toString())) {
            timeService().startBreak(
                    project,
                    jsonObject -> {
                        currentStatus.startBreak(this);
                        enableInteractionAndUpdate();
                    },
                    this::logoutErrorHandler
            );
        } else {
            timeService().stopBreak(
                    project,
                    jsonObject -> {
                        currentStatus.stopBreak(this);
                        enableInteractionAndUpdate();
                    },
                    this::logoutErrorHandler
            );
        }
    }

    protected TextView runningWorked() {
        return findViewById(R.id.time_running_work);
    }

    protected TextView runningBreaks() {
        return findViewById(R.id.time_running_break);
    }

    protected Spinner projectSpinner() {
        return findViewById(R.id.time_project_spinner);
    }

    protected Button startButton() {
        return findViewById(R.id.time_start_button);
    }

    protected LinearLayout secondaryButtonContainer() {
        return findViewById(R.id.time_secondary_buttons);
    }

    protected Button stopButton() {
        return findViewById(R.id.time_stop_button);
    }

    protected Button pauseButton() {
        return findViewById(R.id.time_pause_button);
    }

}
