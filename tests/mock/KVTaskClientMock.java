package mock;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.serializers.EpicSerializer;
import ru.atlassian.jira.serializers.SubtaskSerializer;
import ru.atlassian.jira.serializers.TaskSerializer;
import ru.atlassian.jira.service.HttpTaskServer;
import ru.atlassian.jira.service.KVTaskClient;

public class KVTaskClientMock extends KVTaskClient {
    private Map<Integer, Task> tasks;
    private Map<Integer, Epic> epics;
    private Map<Integer, Epic> subtasks;

    public KVTaskClientMock(String url) throws IOException, InterruptedException {
        super(url);
        tasks = new HashMap<>();
        epics = new HashMap<>();
    }

    @Override
    protected String getToken() {
        return "token";
    }

    @Override
    public void put(String key, String json) {
        Gson gson = getGsonBuilder();
        switch (key) {
            case "tasks":
                Type tasksMapType = new TypeToken<Map<Integer, Task>>() {}.getType();
                tasks = gson.fromJson(json, tasksMapType);
                break;
            case "epics":
                Type epicsMapType = new TypeToken<Map<Integer, Epic>>() {}.getType();
                epics = gson.fromJson(json, epicsMapType);
                break;
            case "subtasks":
                Type subtasksMapType = new TypeToken<Map<Integer, Subtask>>() {}.getType();
                subtasks = gson.fromJson(json, subtasksMapType);
                break;
        }
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
                return gson.toJson(subtasks);
        }
        return "";
    }

    private Gson getGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new HttpTaskServer.LocalDateAdapter());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskSerializer());
        gsonBuilder.registerTypeAdapter(Epic.class, new EpicSerializer());
        gsonBuilder.registerTypeAdapter(Subtask.class, new SubtaskSerializer());
        return gsonBuilder.create();
    }
}
