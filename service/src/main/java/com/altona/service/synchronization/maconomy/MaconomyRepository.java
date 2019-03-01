package com.altona.service.synchronization.maconomy;

import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeData;
import com.altona.service.synchronization.SynchronizationTraceRepository;
import com.altona.service.synchronization.maconomy.model.root.Root;
import com.altona.service.synchronization.maconomy.model.get.Get;
import com.altona.service.synchronization.maconomy.model.init.Init;
import com.altona.service.synchronization.maconomy.model.searchjob.Job;
import com.altona.service.synchronization.maconomy.model.searchproject.Project;
import com.altona.util.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Objects;

import static com.altona.util.ObjectMapperHelper.serialize;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Repository
@AllArgsConstructor
public class MaconomyRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(MaconomyRepository.class);

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private SynchronizationTraceRepository synchronizationTraceRepository;

    public Result<Get, String> timeData(SynchronizeRequest request, MaconomyConfiguration configuration) {
        String url = currentTimeDataUrl(configuration);
        return timeData(request, configuration, url);
    }

    public Result<Get, String> timeData(
            SynchronizeRequest request,
            MaconomyConfiguration configuration,
            LocalDate date,
            String employee
    ) {
        String url = timeDataUrl(configuration, date, employee);
        return timeData(request, configuration, url);
    }

    public Result<Init, String> initTimeData(
            SynchronizeRequest request,
            MaconomyConfiguration configuration,
            LocalDate date,
            String employee,
            String concurrencyControl
    ) {
        HttpHeaders httpHeaders = basicHeaders(configuration);
        httpHeaders.add("Maconomy-Concurrency-Control", concurrencyControl);
        HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
        String url = initTimeDataUrl(configuration, date, employee);
        return executeRequest(request, url, entity, POST, Init.class, "Init ");
    }

    public Result<Project, String> searchProjectData(
            SynchronizeRequest request,
            MaconomyConfiguration configuration,
            MaconomyTimeData maconomyTimeData
    ) {
        HttpHeaders httpHeaders = basicHeaders(configuration);

        String search = "Search Projects";
        String postData = serialize(objectMapper, new Root(maconomyTimeData));
        synchronizationTraceRepository.trace(request, search + " Request", postData);

        HttpEntity<String> entity = new HttpEntity<>(postData, httpHeaders);
        String url = searchProjects(configuration);
        return executeRequest(request, url, entity, POST, Project.class, search);
    }

    public Result<Job, String> searchJobData(
            SynchronizeRequest request,
            MaconomyConfiguration configuration,
            MaconomyTimeData maconomyTimeData
    ) {
        HttpHeaders httpHeaders = basicHeaders(configuration);

        String search = "Search Jobs";
        String postData = serialize(objectMapper, new Root(maconomyTimeData));
        synchronizationTraceRepository.trace(request, search + " Request", postData);

        HttpEntity<String> entity = new HttpEntity<>(postData, httpHeaders);
        String url = searchJobs(configuration);
        return executeRequest(request, url, entity, POST, Job.class, search);
    }

    public Result<Get, String> updateTimeData(
            SynchronizeRequest request,
            MaconomyConfiguration configuration,
            LocalDate date,
            String employee,
            String concurrencyControl,
            int rowNumber,
            MaconomyTimeData maconomyTimeData
    ) {
        HttpHeaders httpHeaders = basicHeaders(configuration);
        httpHeaders.add("Maconomy-Concurrency-Control", concurrencyControl);

        String what = "Update Time Data";
        String postData = serialize(objectMapper, new Root(maconomyTimeData));
        synchronizationTraceRepository.trace(request, what + " Request", postData);

        HttpEntity<String> entity = new HttpEntity<>(postData, httpHeaders);
        String url = updateTimeDataUrl(configuration, date, employee, rowNumber);
        return executeRequest(request, url, entity, POST, Get.class, what);
    }

    public Result<Get, String> writeTimeData(
            SynchronizeRequest request,
            MaconomyConfiguration configuration,
            LocalDate date,
            String employee,
            String concurrencyControl,
            MaconomyTimeData maconomyTimeData
    ) {
        HttpHeaders httpHeaders = basicHeaders(configuration);
        httpHeaders.add("Maconomy-Concurrency-Control", concurrencyControl);

        String what = "New Time Data";
        String postData = serialize(objectMapper, new Root(maconomyTimeData));
        synchronizationTraceRepository.trace(request, what + " Request", postData);

        HttpEntity<String> entity = new HttpEntity<>(postData, httpHeaders);
        String url = writeNewTimeDataUrl(configuration, date, employee);
        return executeRequest(request, url, entity, POST, Get.class, what);
    }

    private Result<Get, String> timeData(SynchronizeRequest request, MaconomyConfiguration configuration, String url) {
        HttpHeaders httpHeaders = basicHeaders(configuration);
        HttpEntity<?> headers = new HttpEntity<>(httpHeaders);
        return executeRequest(request, url, headers, GET, Get.class, "Current Time Data");
    }

    private <T> Result<T, String> executeRequest(SynchronizeRequest request, String url, HttpEntity<?> entity, HttpMethod method, Class<T> clazz, String what) {
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(url, method, entity, JsonNode.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                try {
                    JsonNode body = Objects.requireNonNull(responseEntity.getBody());
                    synchronizationTraceRepository.trace(request, what, body);
                    return Result.success(objectMapper.treeToValue(body, clazz));
                } catch (JsonProcessingException e) {
                    String message = "Couldn't deserialize when " + what;
                    LOGGER.error(message, e);
                    return Result.error(message);
                }
            }
            String message = "Wrong response code received when " + what + " " + responseEntity.getStatusCode();
            LOGGER.error(message);
            return Result.error(message);
        } catch (HttpStatusCodeException e) {
            String message = what + " failed at url " + url + " with code " + e.getStatusCode() + " and body " + e.getResponseBodyAsString();
            LOGGER.error(message, e);
            return Result.error(message);
        } catch (RestClientException e) {
            String message = what + " failed at url " + url;
            LOGGER.error(message, e);
            return Result.error(message);
        }
    }

    private static HttpHeaders basicHeaders(MaconomyConfiguration configuration) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Authorization", authorizationHeader(configuration));
        httpHeaders.add("Maconomy-Format", "date-format=\"M/d/yy\";time-format=\"HH:mm\";thousand-separator=\",\";decimal-separator=\".\";number-of-decimals=2");
        httpHeaders.add("Content-Type", "application/json");
        return httpHeaders;
    }

    private static String authorizationHeader(MaconomyConfiguration configuration) {
        return "Basic " + configuration.getAuthorization();
    }

    private static String currentTimeDataUrl(MaconomyConfiguration configuration) {
        return configuration.getUrl() + "/timeregistration/data;any";
    }

    private static String timeDataUrl(MaconomyConfiguration configuration, LocalDate date, String user) {
        return configuration.getUrl() + "/timeregistration/data;any?" + query(date, user);
    }

    private static String initTimeDataUrl(MaconomyConfiguration configuration, LocalDate date, String user) {
        return configuration.getUrl() + "/timeregistration/data;any/table/init?" + query(date, user);
    }

    private static String searchProjects(MaconomyConfiguration configuration) {
        return configuration.getUrl() + "/timeregistration/data/table/search;foreignkey=jobnumber_jobheader?fields=jobnumber,jobname,customernumber,name1";
    }

    private static String searchJobs(MaconomyConfiguration configuration) {
        return configuration.getUrl() + "/timeregistration/data/table/search;foreignkey=taskname_tasklistline?fields=taskname,description";
    }

    private static String writeNewTimeDataUrl(MaconomyConfiguration configuration, LocalDate date, String user) {
        return configuration.getUrl() + String.format("/timeregistration/data;any/table?%s", query(date, user));
    }

    private static String updateTimeDataUrl(MaconomyConfiguration configuration, LocalDate date, String user, int rowNumber) {
        return configuration.getUrl() + String.format("/timeregistration/data;any/table/%s?%s", rowNumber, query(date, user));
    }

    private static String query(LocalDate date, String user) {
        return String.format("card.datevar=%s&card.employeenumbervar=%s", date, user);
    }

}
