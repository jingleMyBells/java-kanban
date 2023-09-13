package ru.atlassian.jira.service;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Task;

import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private final String filename = "tasks.csv";
    private final String historyFilename = "history.csv";

    FileBackedTasksManager() {
        super();
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
        saveHistory();
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

    private void save() {
        Map<Integer, Task> allTasks = new HashMap<>();
        allTasks.putAll(this.tasks);
        allTasks.putAll(this.epics);
        allTasks.putAll(this.subtasks);
        try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8)) {
            for (Map.Entry<Integer, Task> entry : allTasks.entrySet()) {
                writer.write(entry.getValue().toString() + "\n");
            }
        } catch (IOException exception) {
            System.out.println("ТУТ НАДО ВЫБРОСИТЬ КАСТОМНОЕ ИСКЛЮЧЕНИЕ");
        }
    }

    private void saveHistory() {
        List<Task> history = this.getHistory();
        try (FileWriter writer = new FileWriter(historyFilename, StandardCharsets.UTF_8)) {
            for (Task task : history) {
                writer.write(task.getId() + ",");
            }
        } catch (IOException exception) {
            System.out.println("ТУТ НАДО ВЫБРОСИТЬ КАСТОМНОЕ ИСКЛЮЧЕНИЕ");
        }
    }


    public void restoreFromFile() {
        List<String> tasksToRestore = new ArrayList<>();
        Path path = Paths.get("", filename);

        if (Files.exists(path)) {
            try (FileReader reader = new FileReader(filename, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                while (bufferedReader.ready()) {
                    tasksToRestore.add(bufferedReader.readLine());
                }
            } catch (IOException exception) {
                System.out.println("ТУТ НАДО ВЫБРОСИТЬ КАСТОМНОЕ ИСКЛЮЧЕНИЕ");
            }
        } else {
                System.out.println("Не обнаружено предыдущих сохранений");
        }

        if (!tasksToRestore.isEmpty()) {
            for (String line : tasksToRestore) {
                Task task = getTaskFromString(line);
                switch (task.getClass().toString()) {
                    case ("class ru.atlassian.jira.model.Task"):
                        this.tasks.put(task.getId(), task);
                        break;
                    case ("class ru.atlassian.jira.model.Epic"):
                        this.epics.put(task.getId(), (Epic)task);
                        break;
                    case ("class ru.atlassian.jira.model.Subtask"):
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


    public void restoreHistoryFromFile() {
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
                System.out.println("ТУТ НАДО ВЫБРОСИТЬ КАСТОМНОЕ ИСКЛЮЧЕНИЕ");
            }
        } else {
            System.out.println("Не обнаружено предыдущих сохранений");
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



    public Task getTaskFromString(String value) {
        String[] splitValue = value.split(",");
        Task task = null;
        switch (splitValue[1]) {
            case ("Task"):
                task = new Task(splitValue[2], splitValue[4], Status.valueOf(splitValue[3]));
                break;
            case ("Epic"):
                task = new Epic(splitValue[2], splitValue[4]);
                task.setStatus(Status.valueOf(splitValue[3]));
                break;
            case ("Subtask"):
                task = new Subtask(splitValue[2], splitValue[4], Integer.parseInt(splitValue[5]));
                break;
        }
        if (task != null) {
            task.setId(Integer.parseInt(splitValue[0]));
        }
        return task;
    }


}

