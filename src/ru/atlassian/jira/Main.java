package ru.atlassian.jira;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.service.FileBackedTasksManager;
import ru.atlassian.jira.service.Managers;
import ru.atlassian.jira.service.TaskManager;


public class Main {

    public static void main(String[] args) {
//        myTest();
//        fileTest();
        testPrioritized();
    }

    public static void testPrioritized() {
        TaskManager testManager = Managers.getDefault();
        int duration = 10;
        LocalDateTime start1 = LocalDateTime.of(2023, 1, 1, 1, 0, 0);
        LocalDateTime start2 = LocalDateTime.of(2023, 2, 1, 1, 0, 0);
        LocalDateTime start3 = LocalDateTime.of(2023, 3, 1, 1, 0, 0);

        testManager.createEpic(new Epic("test epic", "sdgf3"));
        Epic epic = testManager.getAllEpics().get(0);


        testManager.createSubtask(new Subtask("1 february", "dfdgjhhdfg", epic.getId(), duration, start2));
        testManager.createSubtask(new Subtask("1 march", "dfdgjhhdfg", epic.getId(), duration, start3));
        testManager.createSubtask(new Subtask("No date", "dfdgjhhdfg", epic.getId()));
        testManager.createSubtask(new Subtask("1 january", "dfdgjhhdfg", epic.getId(), duration, start1));

//        for (Task task : testManager.getPrioritizedTasks()) {
//            System.out.println(task);
//        }
        if (epic.getDuration().isPresent()) {
            System.out.println(epic.getDuration().get());
        } else {
            System.out.println("С длительностью хуйня");
        }





    }

    public static void fileTest() {
        FileBackedTasksManager fileManager = Managers.getFileBacked("tasks.csv");

        Task newTask = new Task("Название без запятых", "Записать задачу в файл", Status.NEW);
        fileManager.createTask(newTask);

        Epic newEpic = new Epic("Тестовый эпик", "КУКлаКОЛДуна");
        fileManager.createEpic(newEpic);

        Subtask newSubtask = new Subtask("Тестовая подзадача", "авпджлоажрд", newEpic.getId());
        fileManager.createSubtask(newSubtask);


        System.out.println("history restored: " + fileManager.getHistory());
        System.out.println("-------");
        System.out.println("все задачи: " + fileManager.getAllTasks());
        System.out.println("все эпики: " + fileManager.getAllEpics());
        System.out.println("все подзадачи: " + fileManager.getAllSubtasks());
        System.out.println("-------");

        Task getSubtask = fileManager.getSubtaskById(3);
        Task getEpic = fileManager.getEpicById(2);
        Task getTask = fileManager.getTaskById(1);

        System.out.println("history modified" + fileManager.getHistory());
        System.out.println("-------");


        if (getTask != null) {
            fileManager.deleteTaskById(getTask.getId());
        }

        System.out.println("все задачи: " + fileManager.getAllTasks());
        System.out.println("все эпики: " + fileManager.getAllEpics());
        System.out.println("все подзадачи: " + fileManager.getAllSubtasks());


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
