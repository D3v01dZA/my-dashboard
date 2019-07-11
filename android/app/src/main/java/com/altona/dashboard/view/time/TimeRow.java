package com.altona.dashboard.view.time;

import android.view.View;
import android.widget.TextView;

import com.altona.dashboard.R;
import com.altona.dashboard.component.UsableChangeNotifier;
import com.altona.dashboard.component.UsableRow;
import com.altona.dashboard.service.time.TimeSummaryEntry;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import static com.altona.dashboard.service.time.TimeService.SHORT_TIME_FORMATTER;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeRow implements UsableRow {

    @NonNull
    private LocalDate date;

    @NonNull
    private LocalTime time;

    public TimeRow(TimeSummaryEntry timeSummaryEntry) {
        this(timeSummaryEntry.getDate(), timeSummaryEntry.getTime());
    }

    @Override
    public int view() {
        return R.layout.time_row;
    }

    @Override
    public void render(View view) {
        view.<TextView>findViewById(R.id.setting_name).setText(date.toString());
        view.<TextView>findViewById(R.id.setting_value).setText(time.format(SHORT_TIME_FORMATTER));
    }

    @Override
    public void onClick(UsableChangeNotifier changeNotifier) {

    }

    @Override
    public void onLongClick(UsableChangeNotifier changeNotifier) {

    }


}
