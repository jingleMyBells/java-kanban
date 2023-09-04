package ru.atlassian.jira;
import java.util.List;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.service.Managers;
import ru.atlassian.jira.service.TaskManager;

public class Main {

    public static void main(String[] args) {
        myTest();
    }

    public static void myTest() {
        TaskManager taskManager = Managers.getDefault();

        Task newTask = new Task("Создать вторую задачу", "Вот тебе первая таска", Status.NEW);
        taskManager.createTask(newTask);
        System.out.println("Тест1");
        Task task = taskManager.getTaskById(1);
        if (task.getTitle().equals("Создать вторую задачу")) {
            System.out.println("Тест1 пройден");
        }

        System.out.println("Тест2");
        Task newTask2 = new Task("Закончить с тасками", "После этого перейди к эпикам", Status.NEW);
        taskManager.createTask(newTask2);

        Task task2 = taskManager.getTaskById(2);

        if (task2.getStatus() == Status.NEW) {
            System.out.println("Тест2 пройден");
        }

        Epic newEpic = new Epic("Первый эпик", "Эпик для двух подзадач");
        taskManager.createEpic(newEpic);

        System.out.println("Тест3");
        Epic epic = taskManager.getEpicById(3);
        if (epic.getStatus() == Status.NEW) {
            System.out.println("Тест3 пройден");
        }


        Subtask subtask1 = new Subtask("Первая подзадача", "Для первого эпика", newEpic.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Вторая подзадача", "Для первого же эпика", newEpic.getId());
        taskManager.createSubtask(subtask2);

        System.out.println("Тест4");
        if (taskManager.getAllSubtasks().contains(subtask1)) {
            System.out.println("Тест4 пройден");
        }

        Epic newEpic2 = new Epic("Второй эпик", "Эпик для одной подзадачи");
        taskManager.createEpic(newEpic2);

        Subtask subtask3 = new Subtask("Третья подзадача", "Для третьего эпика", newEpic2.getId());
        taskManager.createSubtask(subtask3);

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        System.out.println("Тест5");
        if (newEpic.getStatus() == Status.IN_PROGRESS) {
            System.out.println("Тест5 пройден");
        } else {
            System.out.println("Тест5 не пройден");
        }

        taskManager.deleteEpicById(3);

        System.out.println("Тест6");
        if (taskManager.getAllSubtasks().contains(subtask1)) {
            System.out.println("Тест6 не пройден");
        } else {
            System.out.println("Тест6 пройден");
        }

        System.out.println("Тест7");


        taskManager.getTaskById(task.getId());
        taskManager.getSubtaskById(subtask3.getId());
        taskManager.getEpicById(6);
        taskManager.getEpicById(6);

        List<Task> history = taskManager.getHistory();
        int secondTaskIdInHistory = history.get(0).getId();
        int thirdTaskIdInHistory = history.get(1).getId();
        int fifthTaskIdInHistory = history.get(3).getId();
        boolean isTestPassed = (secondTaskIdInHistory == 2) && (thirdTaskIdInHistory == 1)
                && (fifthTaskIdInHistory == 6);

        if (isTestPassed) {
            System.out.println("Тест7 пройден");
        } else {
            System.out.println("Тест7 не пройден");
        }
    }
}
