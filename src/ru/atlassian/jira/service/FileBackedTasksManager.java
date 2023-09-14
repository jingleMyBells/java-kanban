package ru.atlassian.jira.service;

import ru.atlassian.jira.exceptions.ManagerEmptyStorageException;
import ru.atlassian.jira.exceptions.ManagerReadException;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.TaskType;
import ru.atlassian.jira.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {

//    private final String filename = "tasks.csv";
    private final File filename;
//    private final String historyFilename = "history.csv";
    private final File historyFilename;

    FileBackedTasksManager(String filename, String historyFilename) {
        this.filename = new File(filename);
        this.historyFilename = new File(historyFilename);
        restoreFromFile();
        restoreHistoryFromFile();
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
        try {
            saveHistory();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
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
        saveHistory();
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
        saveHistory();
        return subtask;
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    private void save() throws ManagerSaveException {
        Map<Integer, Task> allTasks = new HashMap<>();
        allTasks.putAll(this.tasks);
        allTasks.putAll(this.epics);
        allTasks.putAll(this.subtasks);
        try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8)) {
            for (Map.Entry<Integer, Task> entry : allTasks.entrySet()) {
                writer.write(entry.getValue().toString() + "\n");
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка записи задач на диск");
        }
    }

    private void saveHistory() throws ManagerSaveException {
        List<Task> history = this.getHistory();
        try (FileWriter writer = new FileWriter(historyFilename, StandardCharsets.UTF_8)) {
            for (Task task : history) {
                writer.write(task.getId() + ",");
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка записи истории на диск");
        }
    }


    private void restoreFromFile() throws ManagerReadException, ManagerEmptyStorageException {
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
            throw new ManagerEmptyStorageException("Сохранения не обнаружены");
        }

        if (!tasksToRestore.isEmpty()) {
            for (String line : tasksToRestore) {
                Task task = getTaskFromString(line);
                switch (TaskType.valueOf(task.getClass().getSimpleName().toUpperCase())) {
                    case TASK:
                        this.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        this.epics.put(task.getId(), (Epic)task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        this.subtasks.put(subtask.getId(), subtask);
                        Epic epic = this.epics.get(subtask.getEpicId());
                        epic.addSubtask(subtask);
//                        this.checkAndModifyEpicStatus(epic);
                        break;
                }
            }
        }
    }


    private void restoreHistoryFromFile() throws ManagerReadException, ManagerEmptyStorageException {
        StringBuilder result = new StringBuilder();
        if (historyFilename.exists()) {
            try (FileReader reader = new FileReader(historyFilename, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                while (bufferedReader.ready()) {
                    result.append(bufferedReader.readLine());
                    result.append(",");
                }
            } catch (IOException exception) {
                throw new ManagerReadException("Ошибка чтения истории из файла");
            }
        } else {
            throw new ManagerEmptyStorageException("Сохранения не обнаружены");
        }
        String[] taskIds = String.valueOf(result).split(",");
        for (String id : taskIds) {
            if (!id.isEmpty()) {
                this.historyManager.add(this.tasks.get(Integer.parseInt(id)));
                this.historyManager.add(this.epics.get(Integer.parseInt(id)));
                this.historyManager.add(this.subtasks.get(Integer.parseInt(id)));
            }
        }
    }



    private Task getTaskFromString(String value) {
        String[] splitValue = value.split(",");
        Task task = null;
        switch (TaskType.valueOf(splitValue[1].toUpperCase())) {
            case TASK:
                task = new Task(splitValue[2], splitValue[4], Status.valueOf(splitValue[3]));
                break;
            case EPIC:
                task = new Epic(splitValue[2], splitValue[4]);
                task.setStatus(Status.valueOf(splitValue[3]));
                break;
            case SUBTASK:
                task = new Subtask(splitValue[2], splitValue[4], Integer.parseInt(splitValue[5]));
                task.setStatus(Status.valueOf(splitValue[3]));
                break;
        }
        if (task != null) {
            task.setId(Integer.parseInt(splitValue[0]));
        }
        return task;
    }


}

