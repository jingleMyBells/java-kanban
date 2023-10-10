package ru.atlassian.jira.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeSet;
import java.util.List;

import ru.atlassian.jira.exceptions.ManagerEmptyStorageException;
import ru.atlassian.jira.exceptions.ManagerReadException;
import ru.atlassian.jira.exceptions.ManagerSaveException;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Task;

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
        Gson gson = new Gson();
        String tasks = gson.toJson(this.tasks);
        String epics = gson.toJson(this.epics);
        String subtasks = gson.toJson(this.subtasks);
        try {
            if (kvclient == null) {
                this.kvclient = new KVTaskClient(this.source);
            }
            kvclient.put(Names.tasks, tasks);
            kvclient.put(Names.epics, epics);
            kvclient.put(Names.subtasks, subtasks);
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Возникла ошибка записи в хранилище: " + e.getMessage());
        }
        saveHistory();
    }

    @Override
    protected void saveHistory() throws ManagerSaveException {
        Gson gson = new Gson();
        String history = gson.toJson(getHistory());
        try {
            if (kvclient == null) {
                this.kvclient = new KVTaskClient(this.source);
            }
            kvclient.put(Names.history, history);
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
            System.out.println("восстановление из хранилища бросило исключение " + e.getMessage());
        }
        restoreAutoincrement();
        this.prioritizedTasks = new TreeSet<>(getAllStoredTasks());
    }

    private void loadTasks() throws IOException, InterruptedException {
        Gson gson = new Gson();
        if (kvclient == null) {
            this.kvclient = new KVTaskClient(this.source);
        }
        String dataFromSource = kvclient.load(Names.tasks);
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
        String dataFromSource = kvclient.load(Names.epics);
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
        String dataFromSource = kvclient.load(Names.subtasks);
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
        String dataFromSource = kvclient.load(Names.history);
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

    static class Names {
        public static String tasks = "tasks";
        public static String epics = "epics";
        public static String subtasks = "subtasks";
        public static String history = "history";
    }
}
