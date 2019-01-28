package com.altona.repository.integration.maconomy;

import com.altona.repository.db.time.trace.SynchronizationTraceRepository;
import com.altona.repository.integration.maconomy.create.Post;
import com.altona.repository.integration.maconomy.get.CardData;
import com.altona.repository.integration.maconomy.get.Get;
import com.altona.repository.integration.maconomy.get.TableMeta;
import com.altona.security.Encryptor;
import com.altona.util.functional.Result;
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

@Repository
@AllArgsConstructor
public class MaconomyRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(MaconomyRepository.class);

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private SynchronizationTraceRepository synchronizationTraceRepository;

    public Result<Get, String> timeData(Encryptor encryptor, int userId, int projectId, MaconomyConfiguration maconomyConfiguration) {
        String url = currentTimeDataUrl(maconomyConfiguration);
        return timeData(encryptor, userId, projectId, maconomyConfiguration, url);
    }

    public Result<Get, String> timeData(Encryptor encryptor, int userId, int projectId, MaconomyConfiguration maconomyConfiguration, LocalDate date, String employee) {
        String url = timeDataUrl(maconomyConfiguration, date, employee);
        return timeData(encryptor, userId, projectId, maconomyConfiguration, url);
    }

    private Result<Get, String> timeData(Encryptor encryptor, int userId, int projectId, MaconomyConfiguration maconomyConfiguration, String url) {
        HttpHeaders httpHeaders = basicHeaders(maconomyConfiguration);
        HttpEntity<?> headers = new HttpEntity<>(httpHeaders);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(url, HttpMethod.GET, headers, JsonNode.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                try {
                    JsonNode body = Objects.requireNonNull(responseEntity.getBody());
                    synchronizationTraceRepository.trace(encryptor, userId, projectId, "GET_TIME_DATA", body);
                    return Result.success(objectMapper.treeToValue(body, Get.class));
                } catch (JsonProcessingException e) {
                    String message = "Couldn't deserialize when requesting current data";
                    LOGGER.error(message, e);
                    return Result.error(message);
                }
            }
            String message = "Wrong response code received when requesting current data " + responseEntity.getStatusCode();
            LOGGER.error(message);
            return Result.error(message);
        } catch (HttpStatusCodeException e) {
            String message = "Requesting current data failed at url " + url + " with code " + e.getStatusCode() + " and body " + e.getResponseBodyAsString();
            LOGGER.error(message, e);
            return Result.error(message);
        } catch (RestClientException e) {
            String message = "Requesting current data failed at url " + url;
            LOGGER.error(message, e);
            return Result.error(message);
        }
    }

    public Result<Get, String> writeTimeData(Encryptor encryptor, int userId, int projectId, MaconomyConfiguration maconomyConfiguration, CardData cardData, TableMeta tableMeta, TimeData timeData) {
        Post post = new Post(timeData);
        HttpHeaders httpHeaders = basicHeaders(maconomyConfiguration);
        httpHeaders.add("Maconomy-Concurrency-Control", tableMeta.getConcurrencyControl());
        httpHeaders.add("Content-Type", "application/json");

        String postData = serialize(objectMapper, post);
        synchronizationTraceRepository.trace(encryptor, userId, projectId, "POST_TIME_DATA_REQUEST", postData);

        HttpEntity<String> entity = new HttpEntity<>(postData, httpHeaders);
        String url = updateTimeDataUrl(maconomyConfiguration, cardData, tableMeta);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                try {
                    JsonNode body = Objects.requireNonNull(responseEntity.getBody());
                    synchronizationTraceRepository.trace(encryptor, userId, projectId, "POST_TIME_DATA_RESPONSE", body);
                    return Result.success(objectMapper.treeToValue(body, Get.class));
                } catch (JsonProcessingException e) {
                    String message = "Couldn't deserialize when updating data";
                    LOGGER.error(message, e);
                    return Result.error(message);
                }
            }
            String message = "Response code received when updating data " + responseEntity.getStatusCode();
            LOGGER.error(message);
            return Result.error(message);
        } catch (HttpStatusCodeException e) {
            String message = "Updating data failed at url " + url + " with code " + e.getStatusCode() + " and body " + e.getResponseBodyAsString();
            LOGGER.error(message, e);
            return Result.error(message);
        } catch (RestClientException e) {
            String message = "Updating data failed at url " + url;
            LOGGER.error(message, e);
            return Result.error(message);
        }
    }

    private static HttpHeaders basicHeaders(MaconomyConfiguration maconomyConfiguration) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Authorization", authorizationHeader(maconomyConfiguration));
        httpHeaders.add("Maconomy-Format", format());
        return httpHeaders;
    }

    private static String authorizationHeader(MaconomyConfiguration maconomyConfiguration) {
        return "Basic " + maconomyConfiguration.getAuthorization();
    }

    private static String updateTimeDataUrl(MaconomyConfiguration maconomyConfiguration, CardData cardData, TableMeta tableMeta) {
        return maconomyConfiguration.getUrl() + String.format(
                "/timeregistration/data;any/table/%s?card.datevar=%s&card.employeenumbervar=%s",
                tableMeta.getRowNumber(),
                cardData.getDatevar(),
                cardData.getEmployeenumbervar()
        );
    }

    private static String currentTimeDataUrl(MaconomyConfiguration maconomyConfiguration) {
        return maconomyConfiguration.getUrl() + "/timeregistration/data;any";
    }

    private static String timeDataUrl(MaconomyConfiguration maconomyConfiguration, LocalDate date, String user) {
        return maconomyConfiguration.getUrl() + String.format(
                "/timeregistration/data;any?card.datevar=%s&card.employeenumbervar=%s",
                date,
                user
        );
    }

    private static String format() {
        // Dunno where these apply, they seem to ignore them mostly
        return "date-format=\"M/d/yy\";time-format=\"HH:mm\";thousand-separator=\",\";decimal-separator=\".\";number-of-decimals=2";
    }

}
