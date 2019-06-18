package com.altona.html;

import com.altona.facade.BroadcastFacade;
import com.altona.security.User;
import com.altona.security.UserService;
import com.altona.service.broadcast.Broadcast;
import com.altona.service.broadcast.BroadcastUpdate;
import com.altona.service.project.model.Project;
import com.altona.service.time.model.control.TimeStatus;
import com.altona.service.time.model.summary.TimeSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class BroadcastController {

    private UserService userService;
    private BroadcastFacade broadcastFacade;

    @RequestMapping(path = "/broadcast/update", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Broadcast> update(
            Authentication authentication,
            @RequestBody BroadcastUpdate broadcastUpdate
    ) {
        User user = userService.getUser(authentication);
        return new ResponseEntity<>(broadcastFacade.update(user, broadcastUpdate), HttpStatus.OK);
    }

    @RequestMapping(path = "/broadcast", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Broadcast>> update(
            Authentication authentication
    ) {
        User user = userService.getUser(authentication);
        return new ResponseEntity<>(broadcastFacade.broadcasts(user), HttpStatus.OK);
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
