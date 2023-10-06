package ru.atlassian.jira.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import ru.atlassian.jira.exceptions.KVClientNoTokenAvailable;
import ru.atlassian.jira.exceptions.KVClientWrongStatusCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class KVTaskClient {
    private final String generalUrl;
    private final String token;
    private final HttpClient client;

    public KVTaskClient(String url) throws IOException, InterruptedException {
        this.generalUrl = url;
        this.client = HttpClient.newHttpClient();
        this.token = getToken();
    }

    private String getToken() throws IOException, InterruptedException {
        URI url = URI.create(generalUrl + "/register");
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        HttpRequest request = requestBuilder
                .GET()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html")
                .build();

        String requestedToken = "";

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonElement jsonElement = JsonParser.parseString(response.body());
            requestedToken = jsonElement.getAsString();
        } else {
            throw new KVClientNoTokenAvailable("Не удалось получить токен");
        }

        return requestedToken;
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        URI url = URI.create(generalUrl + "/save/" + key + "?API_TOKEN=" + token);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html")
                .header("Content-Type", "text/html")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new KVClientWrongStatusCode(
                    "При попытке сохранения хранилище ответило с кодом " + response.statusCode()
            );
        }

    }

    public String load(String key) {
        return "1213";
    }

}
