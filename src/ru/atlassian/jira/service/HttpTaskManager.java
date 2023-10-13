package ru.atlassian.jira.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeSet;
import java.util.List;

import ru.atlassian.jira.constants.Constants;
import ru.atlassian.jira.exceptions.ManagerEmptyStorageException;
import ru.atlassian.jira.exceptions.ManagerReadException;
import ru.atlassian.jira.exceptions.ManagerSaveException;
import ru.atlassian.jira.exceptions.MessageException;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.serializers.EpicSerializer;
import ru.atlassian.jira.serializers.SubtaskSerializer;
import ru.atlassian.jira.serializers.TaskSerializer;


public class HttpTaskManager extends FileBackedTasksManager {
    private KVTaskClient kvclient;

    public HttpTaskManager(String url, KVTaskClient client) {
        super(url);
        this.kvclient = client;
    }

    public HttpTaskManager(String url) {
        super(url);
    }

    @Override
    protected void save() throws ManagerSaveException {
        Gson gson = getGsonBuilder();
        String tasks = gson.toJson(this.tasks);
        String epics = gson.toJson(this.epics);
        String subtasks = gson.toJson(this.subtasks);
        try {
            if (kvclient == null) {
                this.kvclient = new KVTaskClient(this.source);
            }
            kvclient.put(Constants.tasks, tasks);
            kvclient.put(Constants.epics, epics);
            kvclient.put(Constants.subtasks, subtasks);
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Возникла ошибка записи в хранилище: " + e.getMessage());
        }
        saveHistory();
    }

    private Gson getGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new HttpTaskServer.LocalDateAdapter());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskSerializer());
        gsonBuilder.registerTypeAdapter(Epic.class, new EpicSerializer());
        gsonBuilder.registerTypeAdapter(Subtask.class, new SubtaskSerializer());
        return gsonBuilder.create();
    }

    @Override
    protected void saveHistory() throws ManagerSaveException {
        Gson gson = getGsonBuilder();
        String history = gson.toJson(getHistory());
        try {
            if (kvclient == null) {
                this.kvclient = new KVTaskClient(this.source);
            }
            kvclient.put(Constants.history, history);
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Возникла ошибка записи в хранилище: " + e.getMessage());
        }
    }

    @Override
    public void restoreFromSource() throws ManagerReadException, ManagerEmptyStorageException {
        try {
            loadTasks();
            loadEpics();
            loadSubtasks();
            loadHistory();
        } catch (IOException | InterruptedException e) {
            throw new MessageException("Восстановление из хранилища бросило исключение " + e.getMessage());
        }
        restoreAutoincrement();
        this.prioritizedTasks = new TreeSet<>(getAllStoredTasks());
    }

    private void loadTasks() throws IOException, InterruptedException {
        Gson gson = new Gson();
        if (kvclient == null) {
            this.kvclient = new KVTaskClient(this.source);
        }
        String dataFromSource = kvclient.load(Constants.tasks);
        Type tasksMapType = new TypeToken<Map<Integer, Task>>() {}.getType();
        Map<Integer, Task> tasksFromSource = gson.fromJson(dataFromSource, tasksMapType);
        if ((tasksFromSource != null) && !tasksFromSource.isEmpty()) {
            this.tasks.putAll(tasksFromSource);
        }
    }

    private void loadEpics() throws IOException, InterruptedException {
        Gson gson = new Gson();
        if (kvclient == null) {
            this.kvclient = new KVTaskClient(this.source);
        }
        String dataFromSource = kvclient.load(Constants.epics);
        Type epicsMapType = new TypeToken<Map<Integer, Epic>>() {}.getType();
        Map<Integer, Epic> epicsFromSource = gson.fromJson(dataFromSource, epicsMapType);
        if ((epicsFromSource != null) && !epicsFromSource.isEmpty()) {
            this.epics.putAll(epicsFromSource);
        }
    }

    private void loadSubtasks() throws IOException, InterruptedException {
        Gson gson = new Gson();
        if (kvclient == null) {
            this.kvclient = new KVTaskClient(this.source);
        }
        String dataFromSource = kvclient.load(Constants.subtasks);
        Type subtasksMapType = new TypeToken<Map<Integer, Subtask>>() {}.getType();
        Map<Integer, Subtask> subtasksFromSource = gson.fromJson(dataFromSource, subtasksMapType);
        if ((subtasksFromSource != null) && !subtasksFromSource.isEmpty()) {
            this.subtasks.putAll(subtasksFromSource);
            for (Subtask subtask : subtasksFromSource.values()) {
                Epic epic = this.epics.get(subtask.getEpicId());
                epic.addSubtask(subtask);
            }
        }
    }

    private void loadHistory() throws IOException, InterruptedException {
        Gson gson = new Gson();
        if (kvclient == null) {
            this.kvclient = new KVTaskClient(this.source);
        }
        String dataFromSource = kvclient.load(Constants.history);
        Type taskListType = new TypeToken<List<Task>>() {}.getType();
        List<Task> historyFromSource = gson.fromJson(dataFromSource, taskListType);
        if ((historyFromSource != null) && !historyFromSource.isEmpty()) {
            for (Task task : historyFromSource) {
                int id = task.getId();
                this.historyManager.add(this.tasks.get(id));
                this.historyManager.add(this.epics.get(id));
                this.historyManager.add(this.subtasks.get(id));
            }
        }
    }
}
