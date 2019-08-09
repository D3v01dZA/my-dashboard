package com.altona.html;

import com.altona.facade.ProjectFacade;
import com.altona.facade.TimeFacade;
import com.altona.service.project.model.Project;
import com.altona.service.time.model.control.TimeStatus;
import com.altona.service.time.model.summary.SummaryType;
import com.altona.service.time.model.summary.TimeSummary;
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
        List<Project> projects = projectFacade.projects(authentication);
        TimeStatus timeStatus = timeFacade.timeStatus(authentication, timeZone);
        if (!projects.isEmpty()) {
            Project project = projects.get(0);
            return timeFacade.summary(
                    authentication,
                    timeZone,
                    project.getId(),
                    SummaryType.CURRENT_WEEK
            ).get().map(
                    summary -> new ResponseEntity<>(new TimeScreen(project, projects, timeStatus, summary), HttpStatus.OK),
                    summaryFailure -> new ResponseEntity<>(summaryFailure.getMessage(), HttpStatus.EXPECTATION_FAILED)
            );
        }
        return new ResponseEntity<>(new TimeScreen(null, projects, timeStatus, null), HttpStatus.OK);
    }


    @AllArgsConstructor
    public static class TimeScreen {

        private Project project;

        @Getter
        @NonNull
        private List<Project> projects;

        @Getter
        @NonNull
        private TimeStatus timeStatus;

        private TimeSummary timeSummary;

        public Optional<Project> getProject() {
            return Optional.ofNullable(project);
        }

        public Optional<TimeSummary> getTimeSummary() {
            return Optional.ofNullable(timeSummary);
        }
    }

}
