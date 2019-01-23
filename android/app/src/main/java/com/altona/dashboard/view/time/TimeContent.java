package com.altona.dashboard.view.time;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.altona.dashboard.GsonHolder;
import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.service.login.LoginService;
import com.altona.dashboard.service.time.Project;
import com.altona.dashboard.service.time.TimeService;
import com.altona.dashboard.service.time.TimeStatus;
import com.altona.dashboard.view.NavigationStatus;
import com.altona.dashboard.view.SecureAppView;
import com.altona.dashboard.view.util.UserInputDialog;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public class TimeContent extends SecureAppView<ViewGroup> {

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();
    private static final LocalTime NO_TIME = LocalTime.of(0, 0);

    private TimeService timeService;

    private TimeStatus currentStatus;
    private Timer timer;

    private TextView runningWorked;
    private TextView runningBreaks;

    private Spinner projectSpinner;

    private Button startButton;

    private LinearLayout secondaryButtonContainer;
    private Button stopButton;
    private Button pauseButton;

    public TimeContent(MainActivity mainActivity, LoginService loginService, Navigation navigation, TimeService timeService) {
        super(mainActivity, loginService, navigation, mainActivity.findViewById(R.id.time_content));
        this.timeService = timeService;
        this.runningWorked = view.findViewById(R.id.time_running_work);
        this.runningBreaks = view.findViewById(R.id.time_running_break);
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

    private void updateStatus() {
        timeService.timeStatus(currentProject(), timeStatus -> {
            setCurrentStatus(timeStatus);
            updateWithCurrentStatus();
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

    private void setCurrentStatus(TimeStatus timeStatus) {
        this.currentStatus = timeStatus;
        updateWithCurrentStatus();
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mainActivity.runOnUiThread(() -> {
                    currentStatus.secondTick();
                    updateWithCurrentStatus();
                });
            }
        }, 1000, 1000);
    }

    private void updateWithCurrentStatus() {
        currentStatus.getStatus().setButtons(this);
        runningWorked.setText("Worked: " + currentStatus.getRunningWorkTotal().orElse(NO_TIME).format(TIME_FORMATTER));
        runningBreaks.setText("Paused: " + currentStatus.getRunningBreakTotal().orElse(NO_TIME).format(TIME_FORMATTER));
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
                    currentStatus.startWork();
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
                    currentStatus.stopWork();
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
                        currentStatus.startBreak();
                        Status.BREAK.setButtons(this);
                        enableInteraction();
                    },
                    this::logoutErrorHandler
            );
        } else {
            timeService.stopBreak(
                    project,
                    jsonObject -> {
                        currentStatus.stopBreak();
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

            @Override
            public boolean addToWorkOnSecondTick() {
                return true;
            }

            @Override
            public boolean addToBreakOnSecondTick() {
                return false;
            }
        },
        BREAK {
            @Override
            public void setButtons(TimeContent timeContent) {
                timeContent.startButton.setVisibility(View.GONE);
                timeContent.secondaryButtonContainer.setVisibility(View.VISIBLE);
                timeContent.pauseButton.setText("Resume");
            }

            @Override
            public boolean addToWorkOnSecondTick() {
                return false;
            }

            @Override
            public boolean addToBreakOnSecondTick() {
                return true;
            }
        },
        NONE {
            @Override
            public void setButtons(TimeContent timeContent) {
                timeContent.startButton.setVisibility(View.VISIBLE);
                timeContent.secondaryButtonContainer.setVisibility(View.GONE);
            }

            @Override
            public boolean addToWorkOnSecondTick() {
                return false;
            }

            @Override
            public boolean addToBreakOnSecondTick() {
                return false;
            }
        };

        public abstract void setButtons(TimeContent timeContent);

        public abstract boolean addToWorkOnSecondTick();

        public abstract boolean addToBreakOnSecondTick();

    }
}
