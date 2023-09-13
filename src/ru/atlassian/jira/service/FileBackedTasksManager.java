package ru.atlassian.jira.service;

import ru.atlassian.jira.exceptions.ManagerEmptyStorageException;
import ru.atlassian.jira.exceptions.ManagerReadException;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.TaskType;
import ru.atlassian.jira.exceptions.ManagerSaveException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private final String filename = "tasks.csv";
    private final String historyFilename = "history.csv";

    FileBackedTasksManager() {
        super();
        try {
            restoreFromFile();
        } catch (ManagerReadException | ManagerEmptyStorageException e) {
            System.out.println(e.getMessage());
        }
        try {
            restoreHistoryFromFile();
        } catch (ManagerReadException | ManagerEmptyStorageException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
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
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        try {
            saveHistory();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
        return epic;
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        try {
            saveHistory();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
        return subtask;
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
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
        Path path = Paths.get("", filename);

        if (Files.exists(path)) {
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
                        this.checkAndModifyEpicStatus(epic);
                        break;
                }
            }
        }
    }


    private void restoreHistoryFromFile() throws ManagerReadException, ManagerEmptyStorageException {
        Path path = Paths.get("", historyFilename);
        StringBuilder result = new StringBuilder();
        if (Files.exists(path)) {
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
                this.historyManager.add(this.getTaskById(Integer.parseInt(id)));
                this.historyManager.add(this.getEpicById(Integer.parseInt(id)));
                this.historyManager.add(this.getSubtaskById(Integer.parseInt(id)));
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
                break;
        }
        if (task != null) {
            task.setId(Integer.parseInt(splitValue[0]));
        }
        return task;
    }


}

