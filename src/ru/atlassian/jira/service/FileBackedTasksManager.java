package ru.atlassian.jira.service;

import ru.atlassian.jira.exceptions.ManagerEmptyStorageException;
import ru.atlassian.jira.exceptions.ManagerReadException;
import ru.atlassian.jira.exceptions.MessageException;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.TaskType;
import ru.atlassian.jira.exceptions.ManagerSaveException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class FileBackedTasksManager extends InMemoryTaskManager {
    protected final String source;
    private final File filename;

    FileBackedTasksManager(String src) {
        this.source = src;
        this.filename = new File(source);
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    protected void save() throws ManagerSaveException {
        Map<Integer, Task> allTasks = getAllStoredObjects();
        try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8)) {
            writer.write("id,type,title,status,description,epicId,duration,startTime\n");
            for (Map.Entry<Integer, Task> entry : allTasks.entrySet()) {
                writer.write(entry.getValue().toString() + "\n");
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка записи задач на диск");
        }
        saveHistory();
    }

    protected void saveHistory() throws ManagerSaveException {
        List<Task> history = this.getHistory();
        if (!history.isEmpty()) {
            List<String> taskIds = new ArrayList<>();
            for (Task task : history) {
                taskIds.add(String.valueOf(task.getId()));
            }
            String result = String.join(",", taskIds);
            try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8, true)) {
                writer.write(result);
            } catch (IOException exception) {
                throw new ManagerSaveException("Ошибка записи истории на диск");
            }
        }

    }

    public void restoreFromSource() throws ManagerReadException, ManagerEmptyStorageException {
        List<String> tasksToRestore = new ArrayList<>();

        if (filename.exists()) {
            try (FileReader reader = new FileReader(filename, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                while (bufferedReader.ready()) {
                    tasksToRestore.add(bufferedReader.readLine());
                }
            } catch (IOException exception) {
                throw new ManagerReadException("Ошибка чтения задач из файла");
            }
        } else {
            System.out.println("Сохранения не обнаружены");
        }

        if (!tasksToRestore.isEmpty()) {
            String lastString = tasksToRestore.get(tasksToRestore.size() - 1);
            String[] lastStringSplitted = lastString.split(",");

            int lastIndex;
            if (lastStringSplitted.length > 1) {
                try {
                    Integer.parseInt(lastStringSplitted[1]);
                    lastIndex = tasksToRestore.size() - 1;

                } catch (NumberFormatException e) {
                    lastIndex = tasksToRestore.size();
                }
            } else {
                lastIndex = tasksToRestore.size() - 1;
            }

            for (int i = 1; i < lastIndex; i++) {
                Task task = getTaskFromString(tasksToRestore.get(i));
                switch (TaskType.valueOf(task.getClass().getSimpleName().toUpperCase())) {
                    case TASK:
                        this.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        this.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        this.subtasks.put(subtask.getId(), subtask);
                        Epic epic = this.epics.get(subtask.getEpicId());
                        epic.addSubtask(subtask);
                        break;
                }
            }

            restoreAutoincrement();

            String[] taskIds = String.valueOf(tasksToRestore.get(tasksToRestore.size() - 1)).split(",");
            for (String id : taskIds) {
                if (!id.isEmpty()) {
                    try {
                        this.historyManager.add(this.tasks.get(Integer.parseInt(id)));
                        this.historyManager.add(this.epics.get(Integer.parseInt(id)));
                        this.historyManager.add(this.subtasks.get(Integer.parseInt(id)));
                    } catch (NumberFormatException exception) {
                        throw new MessageException("Строки с историей нет в исходном файле");
                    }
                }
            }
        }
        this.prioritizedTasks = new TreeSet<>(getAllStoredTasks());
    }

    protected void restoreAutoincrement() {
        Map<Integer, Task> allTasks = getAllStoredObjects();
        int maxId = 0;
        for (Map.Entry<Integer, Task> entry : allTasks.entrySet()) {
            int id = entry.getValue().getId();
            maxId = Integer.max(maxId, id);
        }
        this.autoIncrement = maxId;
    }

    protected Map<Integer, Task> getAllStoredObjects() {
        Map<Integer, Task> allTasks = new HashMap<>();
        allTasks.putAll(this.tasks);
        allTasks.putAll(this.epics);
        allTasks.putAll(this.subtasks);
        return allTasks;
    }

    private Task getTaskFromString(String value) {
        String[] splitValue = value.split(",");
        Task task = null;
        switch (TaskType.valueOf(splitValue[1].toUpperCase())) {
            case TASK:
                if (splitValue[5].equals("0")) {
                    task = new Task(splitValue[2], splitValue[4], Status.valueOf(splitValue[3]));
                } else {
                    task = new Task(
                            splitValue[2],
                            splitValue[4],
                            Status.valueOf(splitValue[3]),
                            Integer.parseInt(splitValue[5]),
                            LocalDateTime.parse(splitValue[6], Task.FORMATTER)
                    );
                }
                break;
            case EPIC:
                task = new Epic(splitValue[2], splitValue[4]);
                task.setStatus(Status.valueOf(splitValue[3]));
                break;
            case SUBTASK:
                if (splitValue[6].equals("0")) {
                    task = new Subtask(splitValue[2], splitValue[4], Integer.parseInt(splitValue[5]));
                    task.setStatus(Status.valueOf(splitValue[3]));
                } else {
                    task = new Subtask(
                            splitValue[2],
                            splitValue[4],
                            Integer.parseInt(splitValue[5]),
                            Integer.parseInt(splitValue[6]),
                            LocalDateTime.parse(splitValue[7], Task.FORMATTER)
                    );
                    task.setStatus(Status.valueOf(splitValue[3]));
                }
                break;
        }
        if (task != null) {
            task.setId(Integer.parseInt(splitValue[0]));
        }
        return task;
    }
}

