package com.altona.service.synchronization.netsuite;

import com.altona.service.synchronization.SynchronizationTraceRepository;
import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.netsuite.model.NetsuiteConfiguration;
import com.altona.service.synchronization.netsuite.model.NetsuiteContext;
import com.altona.service.synchronization.netsuite.model.NetsuiteTimeData;
import com.altona.service.synchronization.netsuite.model.NetsuiteTimeDataList;
import com.altona.util.LocalDateIterator;
import com.altona.util.Result;
import com.google.common.collect.MoreCollectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@AllArgsConstructor
public class NetsuiteBrowser {

    public static final DateTimeFormatter TRANSACTION_DATE_FORMATTER = DateTimeFormatter.ofPattern("d-MMM-yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private SynchronizationTraceRepository synchronizationTraceRepository;

    public Result<NetsuiteContext, String> login(SynchronizeRequest request, NetsuiteConfiguration configuration) {
        NetsuiteContext context = new NetsuiteContext();
        try {
            log.info("Logging in to Netsuite");
            context.get("https://system.netsuite.com/pages/customerlogin.jsp?country=US");
            synchronizationTraceRepository.trace(request, "Before Login", context);

            context.findElement(By.id("userName")).sendKeys(configuration.getUsername());
            context.findElement(By.id("password")).sendKeys(configuration.getPassword());
            context.findElement(By.id("submitButton")).click();

            log.info("Submitting login led to page {}", context.getCurrentUrl());
            if (context.getCurrentUrl().contains("securityquestions")) {
                log.info("Answering security question");
                return answerSecurityQuestion(context, request, configuration);
            }
            return verifyLoggedIn(request, context);
        } catch (Exception ex) {
            log.error("Exception logging in", ex);
            close(context, request);
            return Result.error("Exception occurred while logging in");
        }
    }

    public void weeklyTimesheets(NetsuiteContext context, SynchronizeRequest request) {
        synchronizationTraceRepository.trace(request, "Before Weekly Link Click", context);
        context.findElements(By.className("ns-searchable-value ")).stream()
                .filter(webElement -> webElement.getText().equals("Weekly Timesheet"))
                .collect(MoreCollectors.onlyElement())
                .click();
        log.info("Clicking weekly timesheets led to page {}", context.getCurrentUrl());
    }

    public void previousWeeklyTimesheet(NetsuiteContext context, SynchronizeRequest request) {
        synchronizationTraceRepository.trace(request, "Before Previous Timesheet", context);
        context.findElement(By.id("prev")).click();
        log.info("Clicking previous weekly timesheets led to page {}", context.getCurrentUrl());
    }

    public NetsuiteTimeDataList weeklyData(NetsuiteContext context, SynchronizeRequest request) {
        synchronizationTraceRepository.trace(request, "Before Getting Weekly Data", context);
        log.info("Reading Weekly Time Data");

        LocalDate weekStart = LocalDate.parse(context.findElement(By.id("trandate")).getAttribute("value"), TRANSACTION_DATE_FORMATTER);

        WebElement table = getTable(context);
        List<NetsuiteTimeData> netsuiteTimeDataList = new ArrayList<>();
        int i = 1;
        WebElement row;
        try {
            while ((row = table.findElement(By.id("timeitem_row_" + i++))) != null) {
                List<WebElement> cells = row.findElements(By.tagName("td"));

                Map<LocalDate, LocalTime> daily = new HashMap<>();
                LocalDate current = weekStart;
                for (int j = 16; j < 22; j++) {
                    String time = cells.get(j).getText();
                    if (StringUtils.hasText(time)) {
                        daily.put(current, LocalTime.parse(time, TIME_FORMATTER));
                    }
                    current = current.plusDays(1);
                }

                netsuiteTimeDataList.add(new NetsuiteTimeData(cells.get(0).getText(), cells.get(1).getText(), daily));
            }
        } catch (NoSuchElementException ex) {
            log.info("Row {} not found - ending search for time data", i - 1, ex);
        }
        return new NetsuiteTimeDataList(weekStart, netsuiteTimeDataList);
    }

    public void addLine(NetsuiteContext context, SynchronizeRequest request, LocalDate from, LocalDate to, NetsuiteTimeData data) {
        synchronizationTraceRepository.trace(request, "Before Adding Line", context);
        log.info("Adding Time Data Line");

        context.findElement(By.id("timeitem_customer_display"))
                .click();
        context.findElement(By.id("parent_actionbuttons_timeitem_customer_fs"))
                .click();
        context.findElement(By.id("customer_popup_list"))
                .click();
        context.findElement(By.id("popup_outer_table")).findElements(By.tagName("a")).stream()
                .filter(webElement -> webElement.getText().equals(data.getCustomer()))
                .collect(MoreCollectors.onlyElement())
                .click();

        context.findElement(By.id("timeitem_casetaskevent_display"))
                .click();
        context.findElement(By.id("parent_actionbuttons_timeitem_casetaskevent_fs"))
                .click();
        context.findElement(By.id("casetaskevent_popup_list"))
                .click();
        context.findElement(By.id("popup_outer_table")).findElements(By.tagName("a")).stream()
                .filter(webElement -> webElement.getText().equals(data.getTask()))
                .collect(MoreCollectors.onlyElement())
                .click();

        List<WebElement> cells = context.findElement(By.className("uir-machine-row")).findElements(By.tagName("td"));
        int i = 16;
        for (LocalDate date : LocalDateIterator.exclusive(from, to)) {
            WebElement cell = cells.get(i++);
            cell.click();
            WebElement input = cell.findElement(By.tagName("input"));
            data.getTime(date).ifPresent(
                    time -> input.sendKeys(time.toString())
            );
        }
        synchronizationTraceRepository.trace(request, "Before Submitting Line", context);
        context.findElement(By.id("btn_secondarymultibutton_submitter")).click();
    }

    public void close(NetsuiteContext context, SynchronizeRequest request) {
        synchronizationTraceRepository.trace(request, "Before Close", context);
        context.quit();
    }

    private WebElement getTable(NetsuiteContext context) {
        return context.findElement(By.id("timeitem_splits"));
    }

    private void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Result<NetsuiteContext, String> answerSecurityQuestion(NetsuiteContext context, SynchronizeRequest request, NetsuiteConfiguration configuration) {
        synchronizationTraceRepository.trace(request, "Before Security Question", context);
        WebElement question = context.findElements(By.className("smalltextnolink")).stream()
                .filter(webElement -> webElement.getText().endsWith("?"))
                .collect(MoreCollectors.onlyElement());
        if (question == null) {
            return missingElement(context, "Netsuite security question page did not question at url %s");
        }
        WebElement answer = context.findElement(By.name("answer"));
        if (answer == null) {
            return missingElement(context, "Netsuite security question page did not contain answer input box at url %s");
        }
        WebElement submit = context.findElement(By.name("submitter"));
        if (submit == null) {
            return missingElement(context, "Netsuite security question page did not contain submit button at url %s");
        }
        return configuration.getAnswer(question.getText())
                .map(actualAnswer -> {
                    answer.sendKeys(actualAnswer);
                    submit.click();
                    log.info("Submitting answer led to page {}", context.getCurrentUrl());
                    return verifyLoggedIn(request, context);
                }).orElseGet(() -> {
                    log.info("No answer found");
                    return Result.error(String.format("No answer found for %s", question.getText()));
                });
    }

    private Result<NetsuiteContext, String> verifyLoggedIn(SynchronizeRequest request, NetsuiteContext context) {
        synchronizationTraceRepository.trace(request, "Before Verifying Login", context);
        if (context.getTitle().contains("Home")) {
            return Result.success(context);
        }
        String msg = String.format("Login failed at page %s", context.getCurrentUrl());
        log.info(msg);
        return Result.error(msg);
    }

    private Result<NetsuiteContext, String> missingElement(NetsuiteContext context, String format) {
        context.quit();
        String msg = String.format(format, context.getCurrentUrl());
        log.warn(msg);
        return Result.error(msg);
    }

}
