package ru.atlassian.jira.service;

public class Managers {


    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
    public static FileBackedTasksManager getFileBacked(String filename) {
        return new FileBackedTasksManager(filename);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }


}
