package com.altona.repository.time.maconomy;

import com.altona.repository.time.maconomy.create.Post;
import com.altona.repository.time.maconomy.get.Get;
import com.altona.repository.time.maconomy.get.CardData;
import com.altona.repository.time.maconomy.get.TableMeta;
import com.altona.repository.time.trace.IntegrationTraceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static com.altona.util.ObjectMapperHelper.serialize;

@Repository
@AllArgsConstructor
public class MaconomyRepository {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private IntegrationTraceRepository integrationTraceRepository;

    public Get getCurrentData(int userId, int projectId, MaconomyMetadata maconomyMetadata) {
        HttpHeaders httpHeaders = basicHeaders(maconomyMetadata);
        HttpEntity<?> headers = new HttpEntity<>(httpHeaders);

        String url = currentTimeDataUrl(maconomyMetadata);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(url, HttpMethod.GET, headers, JsonNode.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                try {
                    JsonNode body = Objects.requireNonNull(responseEntity.getBody());
                    integrationTraceRepository.trace(userId, projectId, "GET_TIME_DATA", body);
                    return objectMapper.treeToValue(body, Get.class);
                } catch (JsonProcessingException e) {
                    throw new MaconomyException("Couldn't deserialize when requesting current data", e);
                }
            }
            throw new MaconomyException("Response code received when requesting current data " + responseEntity.getStatusCode());
        } catch (HttpStatusCodeException e) {
            throw new MaconomyException("Requesting current data failed at url " + url + " with code " + e.getStatusCode() + " and body " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new MaconomyException("Requesting current data failed at url " + url);
        }
    }

    public Get writeCurrentData(int userId, int projectId, MaconomyMetadata maconomyMetadata, CardData cardData, TableMeta tableMeta, TimeData timeData) {
        Post post = new Post(timeData);
        HttpHeaders httpHeaders = basicHeaders(maconomyMetadata);
        httpHeaders.add("Maconomy-Concurrency-Control", tableMeta.getConcurrencyControl());
        httpHeaders.add("Content-Type", "application/json");

        String postData = serialize(objectMapper, post);
        integrationTraceRepository.trace(userId, projectId, "POST_TIME_DATA_REQUEST", postData);

        HttpEntity<String> entity = new HttpEntity<>(postData, httpHeaders);
        String url = updateTimeDataUrl(maconomyMetadata, cardData, tableMeta);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                try {
                    JsonNode body = Objects.requireNonNull(responseEntity.getBody());
                    integrationTraceRepository.trace(userId, projectId, "POST_TIME_DATA_RESPONSE", body);
                    return objectMapper.treeToValue(body, Get.class);
                } catch (JsonProcessingException e) {
                    throw new MaconomyException("Couldn't deserialize when writing data", e);
                }
            }
            throw new MaconomyException("Response code received when requesting current data " + responseEntity.getStatusCode());
        } catch (HttpStatusCodeException e) {
            throw new MaconomyException("Writing data failed at url " + url + " with code " + e.getStatusCode() + " and body " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new MaconomyException("Writing data failed at url " + url);
        }
    }

    private static HttpHeaders basicHeaders(MaconomyMetadata maconomyMetadata) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Authorization", authorizationHeader(maconomyMetadata));
        httpHeaders.add("Maconomy-Format", format());
        return httpHeaders;
    }

    private static String authorizationHeader(MaconomyMetadata maconomyMetadata) {
        return "Basic " + maconomyMetadata.getAuthorization();
    }

    private static String updateTimeDataUrl(MaconomyMetadata maconomyMetadata, CardData cardData, TableMeta tableMeta) {
        return maconomyMetadata.getUrl() + String.format(
                "/timeregistration/data;any/table/%s?card.datevar=%s&card.employeenumbervar=%s",
                tableMeta.getRowNumber(),
                cardData.getDatevar(),
                cardData.getEmployeenumbervar()
        );
    }

    private static String currentTimeDataUrl(MaconomyMetadata maconomyMetadata) {
        return maconomyMetadata.getUrl() + "/timeregistration/data;any";
    }

    private static String format() {
        // Dunno where these apply, they seem to ignore them mostly
        return "date-format=\"M/d/yy\";time-format=\"HH:mm\";thousand-separator=\",\";decimal-separator=\".\";number-of-decimals=2";
    }

}
