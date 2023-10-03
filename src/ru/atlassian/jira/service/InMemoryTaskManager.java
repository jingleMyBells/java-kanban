package ru.atlassian.jira.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Optional;

import ru.atlassian.jira.exceptions.TaskNotFoundException;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.exceptions.ManagerInvalidTimePropertiesException;

public class InMemoryTaskManager implements TaskManager {
    protected int autoIncrement;
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;
    protected Set<Task> prioritizedTasks;

    InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedTasks = new TreeSet<>();
    }

    @Override
    public void createTask(Task task) throws ManagerInvalidTimePropertiesException {
        Optional<Task> intersectedTask = taskIntersections(task);
        if (intersectedTask.isPresent()) {
            throw new ManagerInvalidTimePropertiesException(
                    "Задача пересекается по времени с задачей номер" + intersectedTask.get().getId()
            );
        }
        task.setId(createNewTaskId());
        this.tasks.put(task.getId(), task);
        this.prioritizedTasks.add(task);
    }

    @Override
    public void updateTask(Task task) throws ManagerInvalidTimePropertiesException {
        Task taskForUpdate = this.tasks.get(task.getId());
        if (taskForUpdate != null) {
            Optional<Task> intersectedTask = taskIntersections(task);
            if (intersectedTask.isPresent()) {
                throw new ManagerInvalidTimePropertiesException(
                        "Задача пересекается по времени с задачей номер" + intersectedTask.get().getId()
                );
            }
            taskForUpdate.setTitle(task.getTitle());
            taskForUpdate.setDescription(task.getDescription());
            taskForUpdate.setStatus(task.getStatus());
            this.prioritizedTasks.remove(task);
            this.prioritizedTasks.add(task);
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return List.copyOf(this.tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer taskId : this.tasks.keySet()) {
            this.historyManager.remove(taskId);
        }
        this.tasks.clear();
        this.prioritizedTasks = new TreeSet<>(this.subtasks.values());
    }

    @Override
    public Task getTaskById(int id) {
        Task task = this.tasks.get(id);
        this.historyManager.add(task);
        return task;
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = this.tasks.get(id);
        if (task != null) {
            this.prioritizedTasks.remove(task);
        }
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
        if (epicForUpdate != null) {
            epicForUpdate.setTitle(epic.getTitle());
            epicForUpdate.setDescription(epic.getDescription());
        }
    }

    @Override
    public List<Epic> getAllEpics() {
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
        Epic epic = this.epics.get(id);
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
    public List<Subtask> getAllEpicSubtasks(int id) {
        Epic epic = this.epics.get(id);
        if (epic == null) {
            return List.of();
        }
        return epic.getTasks();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        Optional<Task> intersectedTask = taskIntersections(subtask);
        if (intersectedTask.isPresent()) {
            throw new ManagerInvalidTimePropertiesException(
                    "Задача пересекается по времени с задачей номер" + intersectedTask.get().getId()
            );
        }
        Epic epic = this.epics.get(subtask.getEpicId());
        if (epic != null) {
            subtask.setId(createNewTaskId());
            this.subtasks.put(subtask.getId(), subtask);
            epic.addSubtask(subtask);
            this.checkAndModifyEpicStatus(epic);
            this.prioritizedTasks.add(subtask);
        } else {
            throw new TaskNotFoundException("Эпик не найден, подзадача не сохранена");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) throws ManagerInvalidTimePropertiesException {
        Subtask subtaskForUpdate = this.subtasks.get(subtask.getId());
        if (subtaskForUpdate != null) {
            Optional<Task> intersectedTask = taskIntersections(subtask);
            if (intersectedTask.isPresent()) {
                throw new ManagerInvalidTimePropertiesException(
                        "Задача пересекается по времени с задачей номер" + intersectedTask.get().getId()
                );
            }
            subtaskForUpdate.setTitle(subtask.getTitle());
            subtaskForUpdate.setDescription(subtask.getDescription());
            subtaskForUpdate.setStatus(subtask.getStatus());
            Epic epic = this.epics.get(subtask.getEpicId());
            this.checkAndModifyEpicStatus(epic);
            this.prioritizedTasks.remove(subtask);
            this.prioritizedTasks.add(subtask);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return List.copyOf(this.subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer taskId : this.subtasks.keySet()) {
            this.historyManager.remove(taskId);
        }
        this.subtasks.clear();
        for (Epic epic : this.getAllEpics()) {
            epic.getTasks().clear();
            this.checkAndModifyEpicStatus(epic);
        }
        this.prioritizedTasks = new TreeSet<>(this.tasks.values());
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = this.subtasks.get(id);
        this.historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtaskToDelete = this.subtasks.get(id);
        if (subtaskToDelete != null) {
            Epic epic = this.epics.get(subtaskToDelete.getEpicId());
            epic.removeSubtask(this.subtasks.remove(id));
            this.checkAndModifyEpicStatus(epic);
            this.subtasks.remove(id);
            this.historyManager.remove(id);
            this.prioritizedTasks.remove(subtaskToDelete);
        }
    }

    @Override
    public List<Task> getHistory() {
        return this.historyManager.getHistory();
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(this.prioritizedTasks);
    }

    protected int createNewTaskId() {
        return ++this.autoIncrement;
    }

    protected void checkAndModifyEpicStatus(Epic epic) {
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

    protected List<Task> getAllStoredTasks() {
        Map<Integer, Task> allTasks = new HashMap<>();
        allTasks.putAll(this.tasks);
        allTasks.putAll(this.subtasks);
        return new ArrayList<>(allTasks.values());
    }

    protected Optional<Task> taskIntersections(Task taskToValidate) {
        if (taskToValidate.getStartTime().isPresent() && taskToValidate.getEndTime().isPresent()) {
            final LocalDateTime validatedStartTime = taskToValidate.getStartTime().get();
            final LocalDateTime validatedEndTime = taskToValidate.getEndTime().get();
            for (Task task : getAllStoredTasks()) {
                if (task.getStartTime().isEmpty() || task.getEndTime().isEmpty()) {
                    continue;
                }
                if (validatedStartTime.isAfter(task.getEndTime().get())
                        || validatedEndTime.isBefore(task.getStartTime().get())) {
                    continue;
                }
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }
}
