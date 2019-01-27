package com.altona.service.time.synchronize.maconomy;

import com.altona.repository.time.maconomy.MaconomyMetadata;
import com.altona.repository.time.maconomy.MaconomyRepository;
import com.altona.repository.time.maconomy.TimeData;
import com.altona.repository.time.maconomy.get.*;
import com.altona.repository.time.project.Project;
import com.altona.security.UserContext;
import com.altona.service.time.TimeService;
import com.altona.service.time.summary.Summary;
import com.altona.service.time.summary.SummaryConfiguration;
import com.altona.service.time.summary.TimeRounding;
import com.altona.service.time.synchronize.SynchronizeResult;
import com.altona.util.LocalDateIterator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@AllArgsConstructor
public class MaconomyService {

    private TimeService timeService;
    private MaconomyRepository maconomyRepository;

    public SynchronizeResult synchronizeWeek(UserContext userContext, Project project, MaconomyMetadata maconomyMetadata) {
        Get currentTime = maconomyRepository.getCurrentData(userContext.getId(), project.getId(), maconomyMetadata);
        TableRecord tableRecord = currentTime.getTableRecord();
        CardRecord cardRecord = currentTime.getCardRecord();
        CardData cardData = cardRecord.getData();
        TimeData timeData = tableRecord.getData();
        TableMeta tableMeta = tableRecord.getMeta();

        SummaryConfiguration configuration = new SummaryConfiguration(cardData.getPeriodstartvar(), cardData.getPeriodendvar(), TimeRounding.NEAREST_FIFTEEN);
        Summary summary = timeService.summary(userContext, project, configuration);
        rewriteTimes(timeData, summary);

        Get rewrittenTime = maconomyRepository.writeCurrentData(userContext.getId(), project.getId(), maconomyMetadata, cardData, tableMeta, timeData);

        return new SynchronizeResult(false);
    }

    private static void rewriteTimes(TimeData timeData, Summary summary) {
        int current = 0;
        LocalDate fromDate = summary.getFromDate();
        LocalDate toDate = summary.getToDate();
        for (LocalDate date : LocalDateIterator.inclusive(fromDate, toDate)) {
            if (current == 0) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday1(asDecimal(time)));
            } else if (current == 1) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday2(asDecimal(time)));
            } else if (current == 2) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday3(asDecimal(time)));
            } else if (current == 3) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday4(asDecimal(time)));
            } else if (current == 4) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday5(asDecimal(time)));
            } else if (current == 5) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday6(asDecimal(time)));
            } else if (current == 6) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday7(asDecimal(time)));
            } else {
                throw new IllegalStateException("Summary came with more than 7 dates between " + fromDate + " and " + toDate);
            }
            current++;
        }
    }

    private static BigDecimal asDecimal(LocalTime time) {
        // On the site 1:40 becomes 1.6666666666666665 which I have no idea how to replicate so imma ignore it
        String minutes = new BigDecimal(time.getMinute()).divide(new BigDecimal(60), 16, RoundingMode.FLOOR).toPlainString();
        String minutesWithoutZero = minutes.substring(2);
        return new BigDecimal(time.getHour() + "." + minutesWithoutZero);
    }

}
