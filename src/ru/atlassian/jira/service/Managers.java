package ru.atlassian.jira.service;

public class Managers {


    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
    public static FileBackedTasksManager getFileBacked(String filename, String historyFilename) {
        return new FileBackedTasksManager(filename, historyFilename);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }


}
