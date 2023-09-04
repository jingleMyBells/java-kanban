package ru.atlassian.jira.service;

import java.util.*;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Status;

public class InMemoryTaskManager implements TaskManager {

    private int autoIncrement;
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;

    private final HistoryManager historyManager;


    InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public void createTask(Task task) {
        task.setId(createNewTaskId());
        this.tasks.put(task.getId(), task);
    }

    @Override
    public void updateTask(Task task) {
        Task taskForUpdate = this.tasks.get(task.getId());
        taskForUpdate.setTitle(task.getTitle());
        taskForUpdate.setDescription(task.getDescription());
        taskForUpdate.setStatus(task.getStatus());
    }

    @Override
    public Collection<Task> getAllTasks() {
        return List.copyOf(this.tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer taskId : this.tasks.keySet()) {
            this.historyManager.remove(taskId);
        }
        this.tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = this.tasks.get(id);
        this.historyManager.add(task);
        return task;
    }

    @Override
    public void deleteTaskById(int id) {
        this.tasks.remove(id);
        this.historyManager.remove(id);
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(createNewTaskId());
        this.epics.put(epic.getId(), epic);
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic epicForUpdate = this.epics.get(epic.getId());
        epicForUpdate.setTitle(epic.getTitle());
        epicForUpdate.setDescription(epic.getDescription());
    }

    @Override
    public Collection<Epic> getAllEpics() {
        return List.copyOf(this.epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Integer taskId : this.epics.keySet()) {
            this.historyManager.remove(taskId);
        }
        for (Integer taskId : this.subtasks.keySet()) {
            this.historyManager.remove(taskId);
        }
        this.subtasks.clear();
        this.epics.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = this.epics.get(id);
        this.historyManager.add(epic);
        return epic;
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = this.getEpicById(id);
        if (epic == null) {
            return;
        }
        for (Subtask subtask : epic.getTasks()) {
            int subtaskId = subtask.getId();
            this.subtasks.remove(subtaskId);
            this.historyManager.remove(subtaskId);
        }
        this.epics.remove(id);
        this.historyManager.remove(id);
    }

    @Override
    public ArrayList<Subtask> getAllEpicSubtasks(int id) {
        Epic epic = this.getEpicById(id);
        if (epic == null) {
            return new ArrayList<>();
        }
        return epic.getTasks();
    }

    private void checkAndModifyEpicStatus(Epic epic) {
        int tasksCounter = epic.getTasks().size();
        if (tasksCounter > 0) {
            int newTasks = 0;
            int doneTasks = 0;
            for (Subtask subtask : epic.getTasks()) {
                Status status = subtask.getStatus();
                if (status == Status.DONE) {
                    doneTasks++;
                } else if (status == Status.NEW) {
                    newTasks++;
                }
                if (doneTasks > 0 && newTasks > 0) {
                    epic.setStatus(Status.IN_PROGRESS);
                    break;
                }
            }
            if (doneTasks == tasksCounter) {
                epic.setStatus(Status.DONE);
            } else if (newTasks == tasksCounter) {
                epic.setStatus(Status.NEW);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
        } else {
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public void createSubtask(Subtask subtask) {
        subtask.setId(createNewTaskId());
        this.subtasks.put(subtask.getId(), subtask);
        Epic epic = this.epics.get(subtask.getEpicId());
        epic.addSubtask(subtask);
        this.checkAndModifyEpicStatus(epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask subtaskForUpdate = this.subtasks.get(subtask.getId());
        subtaskForUpdate.setTitle(subtask.getTitle());
        subtaskForUpdate.setDescription(subtask.getDescription());
        subtaskForUpdate.setStatus(subtask.getStatus());
        Epic epic = this.epics.get(subtask.getEpicId());
        this.checkAndModifyEpicStatus(epic);
    }

    @Override
    public Collection<Subtask> getAllSubtasks() {
        return List.copyOf(this.subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer taskId : this.subtasks.keySet()) {
            this.historyManager.remove(taskId);
        }
        this.subtasks.clear();
        for (Epic epic : this.getAllEpics()) {
            ArrayList<Subtask> subtasks = epic.getTasks();
            for (Subtask subtask : subtasks) {
                epic.removeSubtask(subtask);
            }
            this.checkAndModifyEpicStatus(epic);
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = this.subtasks.get(id);
        this.historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void deleteSubtaskById(int id) {
        Epic epic = this.epics.get(this.subtasks.get(id).getEpicId());
        epic.removeSubtask(this.subtasks.remove(id));
        this.checkAndModifyEpicStatus(epic);
        this.historyManager.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return this.historyManager.getHistory();
    }

    private int createNewTaskId() {
        return ++this.autoIncrement;
    }

}
