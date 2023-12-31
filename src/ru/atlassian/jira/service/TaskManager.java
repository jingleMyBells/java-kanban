package ru.atlassian.jira.service;

import java.util.List;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Subtask;

public interface TaskManager {

    void createTask(Task task);

    void updateTask(Task task);

    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    void deleteTaskById(int id);

    void createEpic(Epic epic);

    void updateEpic(Epic epic);

    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpicById(int id);

    void deleteEpicById(int id);

    List<Subtask> getAllEpicSubtasks(int id);

    void createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(int id);

    void deleteSubtaskById(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
