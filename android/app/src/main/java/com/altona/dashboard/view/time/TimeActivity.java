package com.altona.dashboard.view.time;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.altona.dashboard.R;
import com.altona.dashboard.component.UsableRecycler;
import com.altona.dashboard.service.time.Project;
import com.altona.dashboard.service.time.TimeService;
import com.altona.dashboard.service.time.TimeStatus;
import com.altona.dashboard.view.SecureAppActivity;
import com.altona.dashboard.view.util.UserInputDialog;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class TimeActivity extends SecureAppActivity {

    private TimeStatus currentStatus;
    private Project currentProject;
    private List<Project> currentProjects;
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
    public void onHide() {
        if (currentStatus != null) {
            currentStatus = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onShow() {
        retrieveTimeScreen();
    }

    private void retrieveTimeScreen() {
        disableInteraction();
        timeService().getTimeScreen(
                timeScreen -> {
                    currentProject = timeScreen.getProject().orElse(null);
                    currentProjects = timeScreen.getProjects();
                    if (currentProjects.size() == 0) {
                        createProject();
                    } else {
                        projectSpinner().setAdapter(new TimeSpinnerAdapter(this, currentProjects));
                        setCurrentStatus(timeScreen.getTimeStatus());
                        recycler().setup(
                                timeScreen.getTimeSummary().get().getTimes().stream()
                                        .map(TimeRow::new)
                                        .collect(Collectors.toList())
                        );
                        enableInteractionAndUpdate();
                    }
                },
                this::logoutErrorHandler
        );
    }

    private void createProject() {
        UserInputDialog.open(this, "Create a Time Project", "",
                input -> timeService().createProject(
                        new Project(-1, input),
                        serviceResponse -> onEnter(),
                        this::logoutErrorHandler
                ),
                () -> {
                });
    }

    private TimeService timeService() {
        return new TimeService(loginService());
    }

    private Project currentProject() {
        return (Project) projectSpinner().getSelectedItem();
    }

    private void setCurrentStatus(TimeStatus timeStatus) {
        currentStatus = timeStatus;
        TimeNotification.notify(this, timeStatus);
        updateWithCurrentStatus();
        timer = new Timer();
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
        syncButton().setOnClickListener(view -> sync());
        stopButton().setOnClickListener(view -> stop());
        pauseButton().setOnClickListener(view -> pause());
    }

    private void disableInteraction() {
        projectSpinner().setEnabled(false);
        startButton().setEnabled(false);
        syncButton().setEnabled(false);
        stopButton().setEnabled(false);
        pauseButton().setEnabled(false);
    }

    private void enableInteractionAndUpdate() {
        updateWithCurrentStatus();
        projectSpinner().setEnabled(true);
        startButton().setEnabled(true);
        syncButton().setEnabled(true);
        stopButton().setEnabled(true);
        pauseButton().setEnabled(true);
    }

    private void sync() {
        disableInteraction();
        Project project = currentProject();
        timeService().synchronize(
                project,
                synchronizationResults -> {
                    boolean failure = synchronizationResults.stream()
                            .anyMatch(synchronizationResult -> !synchronizationResult.isPending());
                    if (failure) {
                        toast("Synchronization Failed");
                    } else {
                        toast("Synchronization Started");
                    }
                    enableInteractionAndUpdate();
                },
                this::logoutErrorHandler
        );
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

    protected LinearLayout primaryButtonContainer() {
        return findViewById(R.id.time_primary_buttons);
    }

    protected Button startButton() {
        return findViewById(R.id.time_start_button);
    }

    protected Button syncButton() {
        return findViewById(R.id.time_sync_button);
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

    protected UsableRecycler recycler() {
        return findViewById(R.id.recycler_view_time);
    }

    protected List<Project> currentProjects() {
        return currentProjects;
    }

}
