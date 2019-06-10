package com.altona.service.synchronization.maconomy;

import com.altona.service.synchronization.SynchronizationTraceRepository;
import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.maconomy.model.MaconomyContext;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeData;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeDataList;
import com.altona.util.LocalDateIterator;
import com.altona.util.Result;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.altona.util.Util.sleep;

@Slf4j
@Repository
@AllArgsConstructor
public class MaconomyBrowser {

    private static final Pattern WEEK_REGEX = Pattern.compile(".*: (.*) - (.*)");
    private static final DateTimeFormatter HEADING_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final DateTimeFormatter READ_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private SynchronizationTraceRepository synchronizationTraceRepository;

    public Result<MaconomyContext, String> login(SynchronizeRequest request, MaconomyConfiguration configuration) {
        MaconomyContext context = new MaconomyContext();
        try {
            log.info("Logging in to Maconomy");
            context.get(configuration.getUrl());
            synchronizationTraceRepository.trace(request, "Before Login", context);

            context.findElement(By.id("username")).sendKeys(configuration.getUsername());
            context.findElement(By.id("password")).sendKeys(configuration.getPassword());
            context.findElement(By.id("login")).click();

            log.info("Submitting login led to page {}", context.getCurrentUrl());

            return verifyLogin(request, context);
        } catch (Exception ex) {
            log.error("Exception logging in to Maconomy ", ex);
            close(context, request);
            return Result.failure("Exception occurred while logging in");
        }
    }

    public void previousWeeklyTimesheet(MaconomyContext context, SynchronizeRequest request) {
        synchronizationTraceRepository.trace(request, "Before Previous Timesheet", context);
        String heading = context.findElement(By.className("heading")).getText();
        log.info("Going to the previous period from {}", heading);
        context.findElement(By.className("icon-recordarrow-left")).click();
        String periodHeading = new WebDriverWait(context, 30)
                .until(webDriver -> {
                    WebElement headingElement = webDriver.findElement(By.className("heading"));
                    String newHeading = headingElement.getText();
                    if (heading.equals(newHeading)) {
                        throw new NoSuchElementException(String.format("Heading %s is not different to %s", heading, newHeading));
                    }
                    return headingElement.getText();
                });
        log.info("Arrived at period {}", periodHeading);
    }

    public Result<MaconomyTimeDataList, String> weeklyData(MaconomyContext context, SynchronizeRequest request) {
        synchronizationTraceRepository.trace(request, "Before Getting Weekly Data", context);
        log.info("Reading Weekly Time Data");

        String heading = context.findElement(By.className("heading")).getText();
        Matcher matcher = WEEK_REGEX.matcher(heading);
        if (!matcher.matches()) {
            String message = String.format("Heading %s was unparseable", heading);
            log.error(message);
            return Result.failure(message);
        }

        LocalDate weekStart = toLocalDate(matcher.group(1));
        LocalDate weekEnd = toLocalDate(matcher.group(2));

        WebElement grid = context.findElement(By.className("k-grid-content"));

        List<MaconomyTimeData> maconomyTimeDataList = Lists.newArrayList();

        for (WebElement timeRow : grid.findElements(By.tagName("tr"))) {
            if (!"k-grid-norecords".equals(timeRow.getAttribute("class"))) {
                List<WebElement> cells = timeRow.findElements(By.tagName("td"));
                if (cells.size() != 14) {
                    return Result.failure(String.format("Expected 14 cells but found %s", cells));
                }

                // Start at cell 4 + the start index
                Map<LocalDate, LocalTime> timeMap = Maps.newHashMap();
                int i = 4 + (6 - (int) ChronoUnit.DAYS.between(weekStart, weekEnd));
                for (LocalDate localDate : LocalDateIterator.inclusive(weekStart, weekEnd)) {
                    WebElement day = cells.get(i++);
                    List<WebElement> dayInputs = day.findElements(By.tagName("input"));
                    if (dayInputs.size() != 2) {
                        String message = String.format("Expected 2 inputs but found %s on date %s", dayInputs, localDate);
                        log.error(message);
                        return Result.failure(message);
                    }
                    String dayTime = dayInputs.get(1).getAttribute("value");
                    if (!StringUtils.isEmpty(dayTime)) {
                        LocalTime time = LocalTime.parse(dayTime, READ_TIME_FORMATTER);
                        timeMap.put(localDate, time);
                    }
                }

                MaconomyTimeData timeData = new MaconomyTimeData(
                        cells.get(2).findElement(By.tagName("input")).getAttribute("value"),
                        cells.get(3).findElement(By.tagName("input")).getAttribute("value"),
                        timeMap
                );
                maconomyTimeDataList.add(timeData);
            }
        }

        return Result.success(new MaconomyTimeDataList(weekStart, weekEnd, maconomyTimeDataList));
    }

    public Optional<String> addLine(MaconomyContext context, SynchronizeRequest request, LocalDate from, LocalDate to, MaconomyTimeData data) {
        synchronizationTraceRepository.trace(request, "Before Adding Line", context);
        log.info("Adding Time Data Line");

        context.findElement(By.className("action-bar")).findElement(By.tagName("button")).click();
        sleep();

        WebElement grid = new WebDriverWait(context, 30)
                .until(webDriver -> webDriver.findElement(By.className("k-grid-content")));

        WebElement editRow = new WebDriverWait(context, 30)
                .until(webDriver -> grid.findElement(By.className("k-grid-edit-row")));

        List<WebElement> cells = editRow.findElements(By.tagName("td"));

        if (cells.size() != 14) {
            String message = String.format("Expected 14 cells but found %s while editing", cells);
            log.error(message);
            return Optional.of(message);
        }

        cells.get(2).findElement(By.tagName("input")).sendKeys(data.getProjectName());
        sleep();
        cells.get(3).findElement(By.tagName("input")).sendKeys(data.getTaskName());
        sleep();

        int i = 4 + (6 - (int) ChronoUnit.DAYS.between(from, to));
        for (LocalDate localDate : LocalDateIterator.inclusive(from, to)) {
            WebElement dayCell = cells.get(i++);
            Optional<String> result = data.getTime(localDate).flatMap(dayTime -> {
                List<WebElement> dayInputs = dayCell.findElements(By.tagName("input"));
                if (dayInputs.size() != 2) {
                    String message = String.format("Expected 2 inputs but found %s on date %s while editing", dayInputs, localDate);
                    log.error(message);
                    return Optional.of(message);
                }
                dayInputs.get(1).sendKeys(dayTime.format(READ_TIME_FORMATTER));
                sleep();
                return Optional.empty();
            });
            if (result.isPresent()) {
                return result;
            }
        }

        context.findElement(By.tagName("dm-actions-row"))
                .findElement(By.className("icon-save"))
                .click();
        sleep();
        return Optional.empty();
    }

    public void close(MaconomyContext context, SynchronizeRequest request) {
        log.info("Closing");
        synchronizationTraceRepository.trace(request, "Before Close", context);
        context.quit();
    }

    private Result<MaconomyContext, String> verifyLogin(SynchronizeRequest request, MaconomyContext context) {
        synchronizationTraceRepository.trace(request, "Before Verifying Login", context);
        try {
            new WebDriverWait(context, 30)
                    .until(webDriver -> webDriver.findElement(By.className("calendar-text")));
            return Result.success(context);
        } catch (NoSuchElementException ex) {
            String msg = String.format("Login failed at page %s", context.getCurrentUrl());
            log.info(msg);
            close(context, request);
            return Result.failure(msg);
        }
    }

    private LocalDate toLocalDate(String from) {
        if (from.charAt(2) != '/') {
            from = "0" + from;
        }
        if (from.charAt(5) != '/') {
            from = from.substring(0, 3) + "0" + from.substring(3);
        }
        return LocalDate.parse(from, HEADING_TIME_FORMATTER);
    }

}
