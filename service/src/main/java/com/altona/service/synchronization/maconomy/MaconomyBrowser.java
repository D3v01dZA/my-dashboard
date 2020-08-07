package com.altona.service.synchronization.maconomy;

import com.altona.service.synchronization.SynchronizationTraceRepository;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.maconomy.model.MaconomyContext;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeData;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeDataList;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.util.LocalDateIterator;
import com.altona.util.Result;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MoreCollectors;
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

    public Result<MaconomyContext, String> login(SynchronizationAttempt attempt, SynchronizationRequest request, MaconomyConfiguration configuration) {
        MaconomyContext context = null;
        try {
            context = new MaconomyContext();
            log.info("Logging in to Maconomy");
            context.get(configuration.getUrl());
            synchronizationTraceRepository.trace(attempt, request, "Before Login", context);

            context.waitForElement(By.id("username")).sendKeys(configuration.getUsername());
            context.waitForElement(By.id("password")).sendKeys(configuration.getPassword());
            context.waitForElement(By.id("login")).click();

            log.info("Submitting login led to page {}", context.getCurrentUrl());

            return verifyLogin(attempt, request, context);
        } catch (RuntimeException ex) {
            log.error("Exception logging in to Maconomy ", ex);
            close(attempt, context, request);
            return Result.failure("Exception occurred while logging in");
        }
    }

    public void previousWeeklyTimesheet(SynchronizationAttempt attempt, MaconomyContext context, SynchronizationRequest request) {
        synchronizationTraceRepository.trace(attempt, request, "Before Previous Timesheet", context);
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
                    if (!WEEK_REGEX.matcher(newHeading).matches()) {
                        throw new NoSuchElementException(String.format("Heading %s is not yet correct", newHeading));
                    }
                    return headingElement.getText();
                });
        log.info("Arrived at period {}", periodHeading);
    }

    public Result<MaconomyTimeDataList, String> weeklyData(SynchronizationAttempt attempt, MaconomyContext context, SynchronizationRequest request) {
        synchronizationTraceRepository.trace(attempt, request, "Before Getting Weekly Data", context);
        log.info("Reading Weekly Time Data");

        String heading = new WebDriverWait(context, 30)
                .until(webDriver -> {
                    String innerHeading = webDriver.findElement(By.className("heading")).getText();
                    if (!WEEK_REGEX.matcher(innerHeading).matches()) {
                        return null;
                    }
                    return innerHeading;
                });
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
            String clazz = timeRow.getAttribute("class");
            if (clazz != null && !clazz.contains("k-grid-norecords")) {
                List<WebElement> cells = timeRow.findElements(By.tagName("td"));
                if (cells.size() != 16) {
                    return Result.failure(String.format("Expected 16 cells but found %s", cells.size()));
                }

                // Start at cell 4 + the start index
                Map<LocalDate, LocalTime> timeMap = Maps.newHashMap();
                for (LocalDate localDate : LocalDateIterator.inclusive(weekStart, weekEnd)) {
                    int index = 5 + localDate.getDayOfWeek().getValue();
                    WebElement day = cells.get(index);
                    List<WebElement> dayInputs = day.findElements(By.tagName("input"));
                    if (dayInputs.size() != 1) {
                        String message = String.format("Expected 1 inputs but found %s on date %s", dayInputs, localDate);
                        log.error(message);
                        return Result.failure(message);
                    }
                    String dayTime = dayInputs.get(0).getAttribute("value");
                    if (!StringUtils.isEmpty(dayTime)) {
                        LocalTime time = LocalTime.parse(dayTime, READ_TIME_FORMATTER);
                        timeMap.put(localDate, time);
                    }
                }

                MaconomyTimeData timeData = new MaconomyTimeData(
                        cells.get(4).findElement(By.tagName("input")).getAttribute("value"),
                        cells.get(5).findElement(By.tagName("input")).getAttribute("value"),
                        timeMap
                );
                maconomyTimeDataList.add(timeData);
            }
        }

        return Result.success(new MaconomyTimeDataList(weekStart, weekEnd, maconomyTimeDataList));
    }

    public Optional<String> addLine(SynchronizationAttempt attempt, MaconomyContext context, SynchronizationRequest request, LocalDate from, LocalDate to, MaconomyTimeData data) {
        synchronizationTraceRepository.trace(attempt, request, "Before Adding Line", context);
        log.info("Adding Time Data Line");

        context.findElement(By.className("action-bar")).findElement(By.tagName("button")).click();
        sleep();

        WebElement grid = new WebDriverWait(context, 30)
                .until(webDriver -> webDriver.findElement(By.className("k-grid-content")));

        WebElement editRow = new WebDriverWait(context, 30)
                .until(webDriver -> grid.findElement(By.className("k-grid-edit-row")));

        List<WebElement> cells = editRow.findElements(By.tagName("td"));

        if (cells.size() != 16) {
            String message = String.format("Expected 16 cells but found %s while editing", cells.size());
            log.error(message);
            return Optional.of(message);
        }

        cells.get(4).findElement(By.tagName("input")).sendKeys(data.getProjectName());
        sleep();
        cells.get(5).findElement(By.tagName("input")).sendKeys(data.getTaskName());
        sleep();

        for (LocalDate localDate : LocalDateIterator.inclusive(from, to)) {
            int index = 5 + localDate.getDayOfWeek().getValue();
            WebElement dayCell = cells.get(index);
            Optional<String> result = data.getTime(localDate).flatMap(dayTime -> {
                List<WebElement> dayInputs = dayCell.findElements(By.tagName("input"));
                if (dayInputs.size() != 1) {
                    String message = String.format("Expected 1 input but found %s on date %s while editing", dayInputs, localDate);
                    log.error(message);
                    return Optional.of(message);
                }
                String format = dayTime.format(READ_TIME_FORMATTER);
                for (int i = 0; i < format.length(); i++) {
                    dayInputs.get(0).sendKeys(String.valueOf(format.charAt(i)));
                    sleep(100);
                }
                sleep();
                return Optional.empty();
            });
            if (result.isPresent()) {
                return result;
            }
        }

        context.findElement(By.id("save"))
                .click();
        sleep();
        return Optional.empty();
    }

    public void close(SynchronizationAttempt attempt, MaconomyContext context, SynchronizationRequest request) {
        log.info("Closing");
        if (context != null) {
            synchronizationTraceRepository.trace(attempt, request, "Before Close", context);
            try {
                log.info("Trying to log out");
                context.findElement(By.className("icon-settings")).click();
                context.findElements(By.className("dropdown-menu-right")).stream()
                        .flatMap(element -> element.findElements(By.tagName("a")).stream())
                        .filter(element -> element.getText().startsWith("Log Out"))
                        .collect(MoreCollectors.onlyElement())
                        .click();
            } catch (RuntimeException ex) {
                log.error("Error encountered logging out", ex);
            } finally {
                context.quit();
            }
        }
    }

    private Result<MaconomyContext, String> verifyLogin(SynchronizationAttempt attempt, SynchronizationRequest request, MaconomyContext context) {
        synchronizationTraceRepository.trace(attempt, request, "Before Verifying Login", context);
        try {
            new WebDriverWait(context, 30)
                    .until(webDriver -> webDriver.findElement(By.className("calendar-text")));
            return Result.success(context);
        } catch (NoSuchElementException ex) {
            String msg = String.format("Login failed at page %s", context.getCurrentUrl());
            log.info(msg);
            close(attempt, context, request);
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
