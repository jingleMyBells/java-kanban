package ru.atlassian.jira.service;

import java.util.List;
import ru.atlassian.jira.model.Task;

public interface HistoryManager {

    List<Task> getHistory();

    void add(Task task);

    void remove(int id);
}
