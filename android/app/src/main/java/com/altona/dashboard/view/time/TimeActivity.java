package com.altona.dashboard.view.time;

import android.view.View;
import android.view.ViewGroup;
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

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public class TimeActivity extends SecureAppActivity {

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();

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
        TimeNotificationService.schedule(this, timeStatus);
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
            runningWorked().setText("Worked: " + currentStatus.getRunningWorkTotal().format(TIME_FORMATTER));
            runningBreaks().setText("Paused: " + currentStatus.getRunningBreakTotal().format(TIME_FORMATTER));
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

    private TextView runningWorked() {
        return findViewById(R.id.time_running_work);
    }

    private TextView runningBreaks() {
        return findViewById(R.id.time_running_break);
    }

    private Spinner projectSpinner() {
        return findViewById(R.id.time_project_spinner);
    }

    private Button startButton() {
        return findViewById(R.id.time_start_button);
    }

    private LinearLayout secondaryButtonContainer() {
        return findViewById(R.id.time_secondary_buttons);
    }

    private Button stopButton() {
        return findViewById(R.id.time_stop_button);
    }

    private Button pauseButton() {
        return findViewById(R.id.time_pause_button);
    }

    public enum Status {

        WORK {
            @Override
            public void setButtons(TimeActivity timeActivity) {
                timeActivity.startButton().setVisibility(View.GONE);
                timeActivity.secondaryButtonContainer().setVisibility(View.VISIBLE);
                timeActivity.pauseButton().setText("Pause");
            }

            @Override
            public boolean updateWork() {
                return true;
            }

            @Override
            public boolean updateBreak() {
                return false;
            }

            @Override
            public boolean requiresNotification() {
                return true;
            }
        },
        BREAK {
            @Override
            public void setButtons(TimeActivity timeActivity) {
                timeActivity.startButton().setVisibility(View.GONE);
                timeActivity.secondaryButtonContainer().setVisibility(View.VISIBLE);
                timeActivity.pauseButton().setText("Resume");
            }

            @Override
            public boolean updateWork() {
                return false;
            }

            @Override
            public boolean updateBreak() {
                return true;
            }

            @Override
            public boolean requiresNotification() {
                return true;
            }
        },
        NONE {
            @Override
            public void setButtons(TimeActivity timeActivity) {
                timeActivity.startButton().setVisibility(View.VISIBLE);
                timeActivity.secondaryButtonContainer().setVisibility(View.GONE);
            }

            @Override
            public boolean updateWork() {
                return false;
            }

            @Override
            public boolean updateBreak() {
                return false;
            }

            @Override
            public boolean requiresNotification() {
                return false;
            }
        };

        public abstract void setButtons(TimeActivity timeActivity);

        public abstract boolean updateWork();

        public abstract boolean updateBreak();

        public abstract boolean requiresNotification();

    }
}
