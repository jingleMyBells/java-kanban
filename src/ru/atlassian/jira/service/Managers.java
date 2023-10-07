package ru.atlassian.jira.service;

import java.io.IOException;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        try {
            return getHttp("http://localhost:8078");
        } catch (IOException | InterruptedException exception) {
            System.out.println(exception.getMessage() + "| Возвращаю память с сохранением в памяти");
            return new InMemoryTaskManager();
        }
    }

    public static FileBackedTasksManager getFileBacked(String filename) {
        return new FileBackedTasksManager(filename);
    }

    public static InMemoryTaskManager getInMemory() {
        return new InMemoryTaskManager();
    }

    public static HttpTaskManager getHttp(String url) throws IOException, InterruptedException {
            return new HttpTaskManager(url);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
