package com.altona.project.time;

import com.altona.project.Project;
import com.altona.project.time.control.BreakStart;
import com.altona.project.time.control.BreakStop;
import com.altona.project.time.control.WorkStart;
import com.altona.project.time.control.WorkStop;
import com.altona.project.time.view.TimeStatusView;

public interface CurrentTime {

    WorkStart startWork(Project project);

    BreakStart startBreak(Project project);

    BreakStop stopBreak(Project project);

    WorkStop stopWork(Project project);

    TimeStatusView asTimeStatusView();

}
