package mock;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.service.KVTaskClient;
import ru.atlassian.jira.exceptions.KVClientNoTokenAvailable;
import ru.atlassian.jira.exceptions.KVClientWrongStatusCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class KVTaskClientMock extends KVTaskClient {
    private final String generalUrl;
    private final String token;
    private final HttpClient client;
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;

    public KVTaskClientMock(String url) throws IOException, InterruptedException {
        super(url);
        this.generalUrl = url;
        this.client = HttpClient.newHttpClient();
        this.token = getToken();
        tasks = new HashMap<>();
        epics = new HashMap<>();
        emulateTasks();
        emulateEpics();
    }

    @Override
    protected String getToken() throws IOException, InterruptedException {
        return "token";
    }

    @Override
    public void put(String key, String json) {
    }

    @Override
    public String load(String key) {
        Gson gson = new Gson();
        switch (key) {
            case "tasks":
                return gson.toJson(tasks);
            case "epics":
                return gson.toJson(epics);
            case "subtasks":
                return "";
        }
        return "";
    }

    private void emulateTasks() {
        int k = 3;
        for (int i = 1; i <= 3; i++) {
            tasks.put(i, new Task(
                    "title" + i,
                    "description" + i,
                    Status.NEW
            ));
        }
    }

    private void emulateEpics() {
        int k = 3;
        for (int i = 1; i <= 3; i++) {
            epics.put(i, new Epic(
                    "title" + i,
                    "description" + i
            ));
        }
    }
}
