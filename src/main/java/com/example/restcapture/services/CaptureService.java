package com.example.restcapture.services;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaptureService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public byte[] getExtractPdf(String ogrn) throws JsonProcessingException, InterruptedException {
        // Выполняем POST-запрос, чтобы получить первый токен
        String firstToken = getTokenFromPostRequest(ogrn);
        System.out.println("!ТОКЕН 1: " + firstToken);

        // Выполняем GET-запрос на URL результата поиска и получаем новый токен
        String searchResultUrl = "https://egrul.nalog.ru/search-result/" + firstToken;
        String searchResultResponseBody = restTemplate.getForObject(searchResultUrl, String.class);
        System.out.println(searchResultResponseBody);

        // Извлекаем значение "t" из второго ответа JSON
        String secondToken = extractTokenFromSearchResult(searchResultResponseBody);
        System.out.println("!ТОКЕН 2: " + secondToken);

        // Выполняем GET-запросы с новым токеном
        performGetRequestWithDelay("https://egrul.nalog.ru/vyp-status/" + secondToken);
        performGetRequestWithDelay("https://egrul.nalog.ru/vyp-request/" + secondToken);

        // Выполняем финальный запрос для получения PDF
        return restTemplate.getForObject("https://egrul.nalog.ru/vyp-download/" + secondToken, byte[].class);
    }

    private String getTokenFromPostRequest(String ogrn) throws JsonProcessingException {
        String url = "https://egrul.nalog.ru/";
        Map<String, String> jsonToSend = new HashMap<>();
        jsonToSend.put("query", ogrn);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(jsonToSend);
        String response = restTemplate.postForObject(url, request, String.class);
        System.out.println(response);
        return extractTokenFromResponse(response);
    }

    private String extractTokenFromResponse(String response) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("t").asText();
    }

    private String extractTokenFromSearchResult(String searchResultResponseBody) throws JsonProcessingException {
        JsonNode searchResultJsonNode = objectMapper.readTree(searchResultResponseBody);
        JsonNode rows = searchResultJsonNode.get("rows");
        JsonNode firstRow = rows.get(0);
        return firstRow.get("t").asText();
    }

    private void performGetRequestWithDelay(String url) throws InterruptedException {
        String response = restTemplate.getForObject(url, String.class);
        System.out.println(response);
        Thread.sleep(500);
    }
}
