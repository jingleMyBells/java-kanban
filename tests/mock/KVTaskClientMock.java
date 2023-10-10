package mock;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;

import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.service.KVTaskClient;

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
    protected String getToken() {
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
        for (int i = 1; i <= k; i++) {
            tasks.put(i, new Task(
                    "title" + i,
                    "description" + i,
                    Status.NEW
            ));
        }
    }

    private void emulateEpics() {
        int k = 3;
        for (int i = 1; i <= k; i++) {
            epics.put(i, new Epic(
                    "title" + i,
                    "description" + i
            ));
        }
    }
}
