package com.altona.html;

import com.altona.project.time.TimeFacade;
import com.altona.project.ProjectFacade;
import com.altona.project.Project;
import com.altona.project.view.ProjectView;
import com.altona.project.time.view.TimeStatusView;
import com.altona.project.time.view.TimeSummaryView;
import com.altona.project.time.SummaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class InterfaceController {

    private ProjectFacade projectFacade;
    private TimeFacade timeFacade;

    @RequestMapping(path = "/interface/time", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> getTime(
            Authentication authentication,
            TimeZone timeZone
    ) {
        List<ProjectView> projects = projectFacade.projects(authentication, timeZone).stream()
                .map(Project::asView)
                .collect(Collectors.toList());
        TimeStatusView timeStatus = timeFacade.currentTime(authentication, timeZone)
                .asTimeStatusView();
        if (!projects.isEmpty()) {
            ProjectView project = projects.get(0);
            return timeFacade.summary(
                    authentication,
                    timeZone,
                    project.getId(),
                    SummaryType.CURRENT_WEEK
            ).get().map(
                    summary -> new ResponseEntity<>(new TimeScreenView(project, projects, timeStatus, summary.asTimeSummaryView()), HttpStatus.OK),
                    summaryFailure -> new ResponseEntity<>(summaryFailure, HttpStatus.EXPECTATION_FAILED)
            );
        }
        return new ResponseEntity<>(new TimeScreenView(null, projects, timeStatus, null), HttpStatus.OK);
    }


    @AllArgsConstructor
    public static class TimeScreenView {

        private ProjectView project;

        @Getter
        @NonNull
        private List<ProjectView> projects;

        @Getter
        @NonNull
        private TimeStatusView timeStatus;

        private TimeSummaryView timeSummary;

        public Optional<ProjectView> getProject() {
            return Optional.ofNullable(project);
        }

        public Optional<TimeSummaryView> getTimeSummary() {
            return Optional.ofNullable(timeSummary);
        }
    }

}
