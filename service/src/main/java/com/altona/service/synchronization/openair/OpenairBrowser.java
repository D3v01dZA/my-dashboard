package com.altona.service.synchronization.openair;

import com.altona.security.Encryptor;
import com.altona.service.synchronization.SynchronizationException;
import com.altona.service.synchronization.SynchronizationTraceRepository;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.openair.model.OpenairConfiguration;
import com.altona.service.synchronization.openair.model.OpenairContext;
import com.altona.service.synchronization.openair.model.OpenairTimeData;
import com.altona.service.synchronization.openair.model.OpenairTimeDataList;
import com.altona.service.time.model.summary.TimeSummary;
import com.altona.util.LocalDateIterator;
import com.google.common.collect.Maps;
import com.google.common.collect.MoreCollectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
@AllArgsConstructor
public class OpenairBrowser {

    private static final DateTimeFormatter TIMESHEET_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESHEET_NAME_DATE = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final Pattern TIMESHEET_NAME = Pattern.compile("(.*) to (.*)");

    private SynchronizationTraceRepository synchronizationTraceRepository;

    public OpenairContext login(SynchronizationAttempt attempt, SynchronizationRequest request, OpenairConfiguration configuration) throws SynchronizationException {
        OpenairContext context = null;
        try {
            context = new OpenairContext();
            log.info("Logging in to Openair");
            context.get("https://www.openair.com/index.pl");
            synchronizationTraceRepository.trace(attempt, request, "Before Login", context);

            context.waitForElement(By.id("login"))
                    .findElement(By.name("account"))
                    .sendKeys(configuration.getCompanyId());
            context.waitForElement(By.id("login"))
                    .findElement(By.name("username"))
                    .sendKeys(configuration.getUserId());
            context.waitForElement(By.id("login"))
                    .findElement(By.name("password"))
                    .sendKeys(configuration.getPassword());
            context.waitForElement(By.id("login_submit_button")).click();

            context.waitForElement(By.className("navdashboardGrayLarge"));

            return context;
        } catch (RuntimeException ex) {
            log.error("Exception logging in to Openair ", ex);
            SynchronizationException exception;
            if (context != null) {
                exception = SynchronizationException.withScreenshot(context.takeScreenshot(), "Error logging in to Openair");
            } else {
                exception = SynchronizationException.withoutScreenshot("Error logging in to Openair");
            }
            close(attempt, context, request);
            throw exception;
        }
    }

    public OpenairTimeDataList navigateToTimesheet(Encryptor encryptor, SynchronizationAttempt attempt, OpenairContext context, LocalDate date) throws SynchronizationException {
        log.info("Navigating to timesheet for date {}", date);
        createTimesheet(encryptor, attempt, context, date);
        TimeSheetDates timeSheetDates = enterTimesheet(encryptor, attempt, context, date);
        List<OpenairTimeData> rows = context.waitForElement(By.id("timesheet_grid")).findElement(By.tagName("table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream()
                .map(webElement -> readRow(timeSheetDates, webElement))
                .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::of))
                .collect(Collectors.toList());
        return new OpenairTimeDataList(timeSheetDates.getStart(), timeSheetDates.getEnd(), rows);
    }

    public void createLine(OpenairContext context, TimeSummary timeSummary, OpenairConfiguration configuration) throws SynchronizationException {
        List<WebElement> tableRow = context.waitForElement(By.id("timesheet_grid")).findElement(By.tagName("table")).findElement(By.tagName("tbody")).findElement(By.className("gridDataEmptyRow")).findElements(By.tagName("td"));
        Select project = new Select(tableRow.get(1).findElement(By.tagName("select")));
        project.selectByVisibleText(configuration.getProject());
        if (!configuration.getProject().equals(project.getFirstSelectedOption().getText())) {
            throw new SynchronizationException(context.takeScreenshot(), "Project not found in select");
        }
        Select task = new Select(tableRow.get(2).findElement(By.tagName("select")));
        task.selectByVisibleText(configuration.getTask());
        if (!configuration.getTask().equals(task.getFirstSelectedOption().getText())) {
            throw new SynchronizationException(context.takeScreenshot(), "Task not found in select");
        }
        int i = 3;
        for (LocalDate date : LocalDateIterator.inclusive(timeSummary.getFromDate(), timeSummary.getToDate())) {
            WebElement day = tableRow.get(i++);
            timeSummary.getActualTime(date)
                    .ifPresent(time -> writeTime(day, time));
        }
        context.waitForElement(By.id("timesheet_savebutton")).click();
    }

    public void close(SynchronizationAttempt attempt, OpenairContext context, SynchronizationRequest request) {
        log.info("Closing");
        if (context != null) {
            synchronizationTraceRepository.trace(attempt, request, "Before Close", context);
            context.quit();
        }
    }

    private Optional<OpenairTimeData> readRow(TimeSheetDates timeSheetDates, WebElement row) {
        List<WebElement> tableElements = row.findElements(By.tagName("td"));
        String project = new Select(tableElements.get(1).findElement(By.tagName("select"))).getFirstSelectedOption().getText();
        String task = new Select(tableElements.get(2).findElement(By.tagName("select"))).getFirstSelectedOption().getText();
        if (task.equalsIgnoreCase("select...")) {
            return Optional.empty();
        }
        Map<LocalDate, LocalTime> timeData = Maps.newHashMap();
        int i = 3;
        for (LocalDate date : LocalDateIterator.inclusive(timeSheetDates.getStart(), timeSheetDates.getEnd())) {
            readTime(tableElements.get(i++))
                    .ifPresent(time -> timeData.put(date, time));
        }
        return Optional.of(new OpenairTimeData(project, task, timeData));
    }

    private void writeTime(WebElement webElement, LocalTime time) {
        String weirdTime = time.getHour() + "." + (time.getMinute() * 100 / 60);
        webElement.findElement(By.tagName("input")).sendKeys(weirdTime);
    }

    private Optional<LocalTime> readTime(WebElement webElement) {
        String hours = webElement.findElement(By.tagName("input")).getAttribute("value");
        if (hours == null || hours.equals("")) {
            return Optional.empty();
        }
        try {
            BigDecimal bigDecimal = new BigDecimal(hours);
            BigInteger hoursPart = bigDecimal.toBigInteger();
            BigDecimal minutesPart = bigDecimal.subtract(new BigDecimal(hoursPart)).multiply(new BigDecimal(60));
            return Optional.of(LocalTime.of(hoursPart.intValue(), minutesPart.intValue()));
        } catch (NumberFormatException ex) {
            log.warn("Failed to parse {}", hours, ex);
            return Optional.empty();
        }
    }

    private void createTimesheet(Encryptor encryptor, SynchronizationAttempt attempt, OpenairContext context, LocalDate date) {
        log.info("Creating timesheet if necessary");
        synchronizationTraceRepository.trace(attempt, encryptor, "Before Getting To Create", context);

        context.findElements(By.className("nav-list")).stream()
                .map(element -> element.findElement(By.className("item")))
                .filter(webElement -> "Create".equals(webElement.getText()))
                .collect(MoreCollectors.onlyElement())
                .click();

        context.waitForElement(By.className("nav-wrapper-content-shadow"), By.className("item"), "Timesheets: Timesheet, New")
                .click();

        Select select = new Select(context.waitForElement(By.tagName("select")));
        WebElement timesheetDateElement = context.waitForElement(By.tagName("select"))
                .findElements(By.tagName("option")).stream()
                .filter(webElement -> {
                    String value = webElement.getAttribute("value");
                    try {
                        LocalDate timesheetDate = LocalDate.parse(value, TIMESHEET_DATE);
                        if (date.equals(timesheetDate)) {
                            return true;
                        }
                        return date.isAfter(timesheetDate) && ChronoUnit.DAYS.between(timesheetDate, date) < 7;
                    } catch (RuntimeException ex) {
                        log.warn("Exception finding timesheet", ex);
                        return false;
                    }
                }).collect(MoreCollectors.onlyElement());
        select.selectByValue(timesheetDateElement.getAttribute("value"));

        if (!timesheetDateElement.getText().contains("already created")) {
            synchronizationTraceRepository.trace(attempt, encryptor, "Before Create", context);
            context.waitForElement(By.id("formButtonsBottom"))
                    .findElement(By.name("save"))
                    .click();
        }
    }

    private TimeSheetDates enterTimesheet(Encryptor encryptor, SynchronizationAttempt attempt, OpenairContext context, LocalDate date) throws SynchronizationException {
        log.info("Entering timesheet for date {}", date);
        synchronizationTraceRepository.trace(attempt, encryptor, "Before Enter", context);

        context.findElements(By.className("nav-list")).stream()
                .flatMap(element -> element.findElements(By.className("item")).stream())
                .filter(webElement -> "Timesheets".equals(webElement.getText()))
                .collect(MoreCollectors.onlyElement())
                .click();

        context.waitForElements(By.className("nav-wrapper-content-shadow"), By.className("item-subtab"), "Open", 2)
                .get(0).click();

        WebElement sheet = context.waitForElement(By.className("list-view-content"))
                .findElements(By.className("cell-content-value")).stream()
                .flatMap(webElement -> webElement.findElements(By.tagName("a")).stream())
                .filter(webElement -> {
                    Matcher matcher = TIMESHEET_NAME.matcher(webElement.getText());
                    if (matcher.matches()) {
                        LocalDate from = LocalDate.parse(matcher.group(1), TIMESHEET_NAME_DATE);
                        LocalDate to = LocalDate.parse(matcher.group(2), TIMESHEET_NAME_DATE);
                        return from.equals(date) || to.isEqual(date) || (from.isBefore(date) && to.isAfter(date));
                    } else {
                        return false;
                    }
                }).collect(MoreCollectors.onlyElement());
        Matcher matcher = TIMESHEET_NAME.matcher(sheet.getText());
        matcher.matches();
        LocalDate from = LocalDate.parse(matcher.group(1), TIMESHEET_NAME_DATE);
        LocalDate to = LocalDate.parse(matcher.group(2), TIMESHEET_NAME_DATE);

        // Can't click the anchor so just do that manually
        context.get(sheet.getAttribute("href"));
        return new TimeSheetDates(from, to);
    }

    @Getter
    @AllArgsConstructor
    private static class TimeSheetDates {

        @NonNull
        private LocalDate start;

        @NonNull
        private LocalDate end;

    }
}
