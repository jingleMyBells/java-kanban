package ru.atlassian.jira.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.atlassian.jira.service.Managers;
import ru.atlassian.jira.service.TaskManager;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Status;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;



public abstract class TaskManagerTest<T extends TaskManager> {

    public static TaskManager taskManager;

    @BeforeEach
    public void createManager() {
        taskManager = Managers.getDefault();
    }


    @Test
    public void createsNewTaskWithEmptyTaskList() {
        Task firstTask = new Task("Test task1", "Test description", Status.NEW);
        taskManager.createTask(firstTask);
        Task firstTaskGot = taskManager.getTaskById(1);

        assertNotNull(firstTaskGot, "После создания задачи при пустом менеджере возвращается null вместо задачи");
        assertEquals(
                firstTaskGot.getTitle(),
                firstTask.getTitle(),
                "После создания задачи при пустом списке название задачи неожиданное"
        );

    }

    @Test
    public void createsNewTask() {
        Task firstTask = new Task("Test task1", "Test description", Status.NEW);
        taskManager.createTask(firstTask);
        Task secondTask = new Task("Test task2", "Test description", Status.NEW);
        taskManager.createTask(secondTask);
        List<Task> tasks = taskManager.getAllTasks();

        assertEquals(
                tasks.size(),
                2,
                "При штатном создании задачи итоговый список задач неожиданной длины"
        );

    }


    @Test
    public void createsNewTaskWithIncorrectId() {
        String title = "Test task1";
        Task firstTask = new Task(title, "Test description", Status.NEW);
        taskManager.createTask(firstTask);
        String anotherTitle = "Test task2";
        Task secondTask = new Task(anotherTitle, "Test description", Status.NEW);
        secondTask.setId(1);
        taskManager.createTask(secondTask);

        Task firstTaskGot = taskManager.getTaskById(1);
        Task secondTaskGot = taskManager.getTaskById(2);

        assertNotNull(
                firstTaskGot,
                "При создании задачи с некорректным ID первая задача не найдена"
        );
        assertNotNull(
                secondTaskGot,
                "При создании задачи с некорректным ID  вторая задача не найдена"
        );

        assertEquals(
                firstTaskGot.getTitle(),
                title,
                "При создании задачи с некорректным ID у первой задачи некорректное название"
        );
        assertEquals(
                secondTaskGot.getTitle(),
                anotherTitle,
                "При создании задачи с некорректным ID у второй задачи некорректное название"
        );
    }

    @Test
    public void updatesTask() {
        Task firstTask = new Task("Test task1", "Test description", Status.NEW);
        taskManager.createTask(firstTask);

        Task firstTaskGot = taskManager.getTaskById(1);
        firstTaskGot.setStatus(Status.DONE);
        taskManager.updateTask(firstTaskGot);

        assertEquals(
                taskManager.getTaskById(1).getStatus(),
                Status.DONE,
                "При обновлении статуса задачи он не изменился в менеджере"
        );

    }

    @Test
    public void updatesNonExistingTask() {
        String title1 = "Test task1";
        Task firstTask = new Task(title1, "Test description", Status.NEW);
        taskManager.createTask(firstTask);

        String title2 = "Test task";
        Task task = new Task(title2, "Test description", Status.NEW);
        taskManager.updateTask(task);

        assertNotEquals(
                taskManager.getTaskById(1).getTitle(),
                title2,
                "При обновлении несуществующей задачи изменилась существующая задача"
        );
    }

    @Test
    public void getsAllTasks() {
        Task firstTask = new Task("Test task1", "Test description", Status.NEW);
        Task secondTask = new Task("Test task2", "Test description", Status.NEW);
        Task thirdTask = new Task("Test task3", "Test description", Status.NEW);

        Task[] tasks = new Task[] {firstTask, secondTask, thirdTask};

        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createTask(thirdTask);

        List<Task> tasksInManager = taskManager.getAllTasks();

        for (int i = 0; i < tasksInManager.size(); i++) {
            assertEquals(
                    tasksInManager.get(i).getTitle(),
                    tasks[i].getTitle(),
                    "При обходе всех задач из менеджера названия назад неожиданные"
            );
        }

    }

    @Test
    public void getsAllTasksWithEmptyTaskList() {
        assertTrue(
                taskManager.getAllTasks().isEmpty(),
                "При отсутствии задач менеджер вернул не пустой список задач"
        );
    }

    @Test
    public void deletesAllTasks() {
        Task firstTask = new Task("Test task1", "Test description", Status.NEW);
        Task secondTask = new Task("Test task2", "Test description", Status.NEW);
        Task thirdTask = new Task("Test task3", "Test description", Status.NEW);

        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createTask(thirdTask);

        assertFalse(
                taskManager.getAllTasks().isEmpty(),
                "При наличии задач менеджер вернул пустой список"
        );

        taskManager.deleteAllTasks();

        assertTrue(
                taskManager.getAllTasks().isEmpty(),
                "После удаления всех задач менерджер верну не пустой список"
        );


    }

    @Test
    public void getsTaskById() {
        Task task = new Task("Test title", "Test descrition", Status.NEW);
        taskManager.createTask(task);

        Task taskGot = taskManager.getTaskById(1);

        assertNotNull(
                taskGot,
                "При попытке получить задачу по ID менежер вернул null"
        );
        assertEquals(
                task.getTitle(),
                taskGot.getTitle(),
                "При попытке получить задачу по ID менеджер вернул задачу с неожиданным названием"
        );

    }

    @Test
    public void getsNonExistingTaskById() {
        Task task = new Task("Test title", "Test descrition", Status.NEW);
        taskManager.createTask(task);

        Task taskGot = taskManager.getTaskById(2);

        assertNull(
                taskGot,
                "При попытке получить несуществующую задачу по ID менеджер вернул не bull"
        );

    }

    @Test
    public void deletesTaskById() {
        Task task = new Task("Test title", "Test descrition", Status.NEW);
        taskManager.createTask(task);

        taskManager.deleteTaskById(1);

        Task taskGot = taskManager.getTaskById(1);

        assertNull(
                taskGot,
                "При удалении задачи по ID задача осталась в менеджере"
        );
    }

    @Test
    public void deletesNonExistingTaskById() {
        Task task = new Task("Test title", "Test descrition", Status.NEW);
        taskManager.createTask(task);

        int tasksSizeBefore = taskManager.getAllTasks().size();

        taskManager.deleteTaskById(2);

        int tasksSizeAfter = taskManager.getAllTasks().size();

        assertEquals(
                tasksSizeBefore,
                tasksSizeAfter,
                "При удалении несуществующей задачи по id размер списка задач в менеджере изменился"
        );

    }

    @Test
    public void createsEpic() {
        Epic firstEpic = new Epic("Test epic1", "Test description");
        taskManager.createEpic(firstEpic);
        Epic secondEpic = new Epic("Test epic2", "Test description");
        taskManager.createEpic(secondEpic);
        List<Epic> epics = taskManager.getAllEpics();

        assertEquals(
                epics.size(),
                2,
                "При штатном создании задачи итоговый список эпиков неожиданной длины"
        );

    }

    @Test
    public void createsEpicWithEmptyEpicList() {
        Epic firstEpic = new Epic("Test task1", "Test description");
        taskManager.createEpic(firstEpic);
        Epic firstEpicGot = taskManager.getEpicById(1);

        assertNotNull(firstEpicGot, "После создания эпика при пустом менеджере возвращается null вместо эпика");
        assertEquals(
                firstEpicGot.getTitle(),
                firstEpic.getTitle(),
                "После создания эпика при пустом списке название эпика неожиданное"
        );

    }

    @Test
    public void createsEpicWithIncorrectId() {
        String title = "Test epic1";
        Epic firstEpic = new Epic(title, "Test description");
        taskManager.createEpic(firstEpic);
        String anotherTitle = "Test epic2";
        Epic secondEpic = new Epic(anotherTitle, "Test description");
        secondEpic.setId(1);
        taskManager.createEpic(secondEpic);

        Task firstEpicGot = taskManager.getEpicById(1);
        Task secondEpicGot = taskManager.getEpicById(2);

        assertNotNull(
                firstEpicGot,
                "При создании эпика с некорректным ID первый эпик не найден"
        );
        assertNotNull(
                secondEpicGot,
                "При создании эпика с некорректным ID  второй эпик не найден"
        );

        assertEquals(
                firstEpicGot.getTitle(),
                title,
                "При создании эпика с некорректным ID у первого эпика некорректное название"
        );
        assertEquals(
                secondEpicGot.getTitle(),
                anotherTitle,
                "При создании эпика с некорректным ID у второго эпика некорректное название"
        );
    }

    @Test
    public void updatesEpic() {
        Epic firstEpic = new Epic("Test epic1", "Test description");
        taskManager.createEpic(firstEpic);

        Epic firstEpicGot = taskManager.getEpicById(1);
        firstEpicGot.setDescription("lalala");
        taskManager.updateEpic(firstEpicGot);

        assertEquals(
                taskManager.getEpicById(1).getDescription(),
                "lalala",
                "При обновлении описания эпика оно не изменилось в менеджере"
        );
    }

    @Test
    public void updatesNonExistingEpic() {
        String title1 = "Test epic1";
        Epic firstEpic = new Epic(title1, "Test description");
        taskManager.createEpic(firstEpic);

        String title2 = "Test task";
        Epic epic = new Epic(title2, "Test description");
        taskManager.updateEpic(epic);

        assertNotEquals(
                taskManager.getEpicById(1).getTitle(),
                title2,
                "При обновлении несуществующего эпика изменился существующий эпик"
        );
    }

    @Test
    public void getsAllTEpics() {
        Epic firstEpic = new Epic("Test epic1", "Test description");
        Epic secondEpic = new Epic("Test epic2", "Test description");
        Epic thirdEpic = new Epic("Test epic3", "Test description");

        Epic[] epics = new Epic[] {firstEpic, secondEpic, thirdEpic};

        taskManager.createTask(firstEpic);
        taskManager.createTask(secondEpic);
        taskManager.createTask(thirdEpic);

        List<Epic> epicsInManager = taskManager.getAllEpics();

        for (int i = 0; i < epicsInManager.size(); i++) {
            assertEquals(
                    epicsInManager.get(i).getTitle(),
                    epics[i].getTitle(),
                    "При обходе всех эпиков из менеджера названия эпиков неожиданные"
            );
        }
    }

    @Test
    public void getsAllTasksWithEmptyEpicList() {
        assertTrue(
                taskManager.getAllEpics().isEmpty(),
                "При отсутствии эпиков менеджер вернул не пустой список эпиков"
        );
    }

    @Test
    public void deletesAllEpics() {
        Epic firstEpic = new Epic("Test epic1", "Test description");
        Epic secondEpic = new Epic("Test epic2", "Test description");
        Epic thirdEpic = new Epic("Test epic3", "Test description");

        taskManager.createEpic(firstEpic);
        taskManager.createEpic(secondEpic);
        taskManager.createEpic(thirdEpic);

        Epic firstEpicGot = taskManager.getEpicById(1);

        Subtask subtask = new Subtask("Test subtask1", "Test description", firstEpicGot.getId());
        taskManager.createSubtask(subtask);

        assertFalse(
                taskManager.getAllEpics().isEmpty(),
                "При наличии эпиков менеджер вернул пустой список"
        );

        int subtaskCountBefore = taskManager.getAllSubtasks().size();

        taskManager.deleteAllEpics();

        assertTrue(
                taskManager.getAllTasks().isEmpty(),
                "После удаления всех эпиков менерджер вернул не пустой список"
        );

        int subtaskCountAfter = taskManager.getAllSubtasks().size();

        assertNotEquals(
                subtaskCountBefore,
                subtaskCountAfter,
                "При удалении всех эпиков количество подзадач до и после удаление совпало"
        );

    }

    @Test
    public void getsEpicById() {
        Epic epic = new Epic("Test title", "Test descrition");
        taskManager.createEpic(epic);

        Epic epicGot = taskManager.getEpicById(1);

        assertNotNull(
                epicGot,
                "При попытке получить эпик по ID менежер вернул null"
        );
        assertEquals(
                epic.getTitle(),
                epicGot.getTitle(),
                "При попытке получить эпик по ID менеджер вернул эпик с неожиданным названием"
        );
    }

    @Test
    public void getsNonExistingEpicById() {
        Epic epic = new Epic("Test title", "Test descrition");
        taskManager.createEpic(epic);

        Epic epicGot = taskManager.getEpicById(2);

        assertNull(
                epicGot,
                "При попытке получить несуществующий эпик по ID менеджер вернул не null"
        );
    }

    @Test
    public void deletesEpicById() {
        Epic epic = new Epic("Test title", "Test descrition");
        taskManager.createEpic(epic);

        Epic epicGot = taskManager.getEpicById(1);

        Subtask subtask = new Subtask("Test title", "Test description", epicGot.getId());
        taskManager.createSubtask(subtask);

        taskManager.deleteEpicById(1);

        epicGot = taskManager.getEpicById(1);

        assertNull(
                epicGot,
                "При удалении эпика по ID эпик осталась в менеджере"
        );

        assertEquals(
                taskManager.getAllSubtasks().size(),
                0,
                "После удаления эпика по ID подзадача не удалилась"
        );
    }

    @Test
    public void deletesNonExistingEpicById() {
        Epic epic = new Epic("Test title", "Test description");
        taskManager.createEpic(epic);

        Epic epicGot = taskManager.getEpicById(1);

        Subtask subtask = new Subtask("Test title", "Test description", epicGot.getId());
        taskManager.createSubtask(subtask);


        int epicsSizeBefore = taskManager.getAllEpics().size();
        int subtasksSizeBefore = taskManager.getAllSubtasks().size();

        taskManager.deleteEpicById(2);

        int epicsSizeAfter = taskManager.getAllEpics().size();
        int subtasksSizeAfter = taskManager.getAllSubtasks().size();

        assertEquals(
                epicsSizeBefore,
                epicsSizeAfter,
                "При удалении несуществующего эпика по id размер списка эпиков в менеджере изменился"
        );
        assertEquals(
                subtasksSizeBefore,
                subtasksSizeAfter,
                "При удалении несуществующего эпика по id размер списка эпиков в менеджере изменился"
        );
    }

    @Test
    public void getsAllEpicSubtasks() {
        Epic epic = new Epic("Test title", "Test description");
        taskManager.createEpic(epic);
        Epic epicGot = taskManager.getEpicById(1);

        Epic epic2 = new Epic("Test title", "Test description");
        taskManager.createEpic(epic2);
        Epic epic2Got = taskManager.getEpicById(2);

        String title = "Test title";
        String title2 = "Test title2";
        String title3 = "Test title3";
        String title4 = "Test title4";

        Subtask subtask = new Subtask(title, "Test description", epicGot.getId());
        taskManager.createSubtask(subtask);

        Subtask subtask2 = new Subtask(title2, "Test description", epicGot.getId());
        taskManager.createSubtask(subtask2);

        Subtask subtask3 = new Subtask(title3, "Test description", epicGot.getId());
        taskManager.createSubtask(subtask3);

        Subtask subtask4  = new Subtask(title4, "Test description", epic2Got.getId());
        taskManager.createSubtask(subtask4);

        assertNotEquals(
                taskManager.getAllEpicSubtasks(epicGot.getId()).size(),
                taskManager.getAllEpicSubtasks(epic2Got.getId()).size(),
                "При получении списков подзадач эпиков размеры большого и малого эпиков совпали"
        );

        assertEquals(
                taskManager.getAllEpicSubtasks(epicGot.getId()).get(0).getTitle(),
                title,
                "При получении всех подаздач название первой подзадачи первого эпика не совпало с ожидаемым"
        );

        assertEquals(
                taskManager.getAllEpicSubtasks(epic2Got.getId()).get(0).getTitle(),
                title4,
                "При получении всех подзадач название первой подзадачи второго эпика не совпало с ожидаемым"
        );


    }

    @Test
    public void getsAllNonExistingEpicSubtasks() {
         assertTrue(
                taskManager.getAllEpicSubtasks(1).isEmpty(),
                "При получении всех подзадач несуществующего эпика список вернулся не пустым"
                );
    }

    @Test
    public void createsNewSubtask() {
        Epic epic = new Epic("Test epic1", "Test description");
        taskManager.createEpic(epic);

        Epic epicGot = taskManager.getEpicById(1);

        String title = "Test subtask";

        Subtask subtask = new Subtask(title, "Test description", epicGot.getId());
        taskManager.createSubtask(subtask);

        assertFalse(
                taskManager.getAllSubtasks().isEmpty(),
                "После создания подзадачи список подзадач пуст"
        );

        assertEquals(
                taskManager.getAllEpicSubtasks(1).get(0).getTitle(),
                title,
                "После создания подзадачи название подзадачи в менеджере отличается от ожидаемого"
        );

    }

    @Test
    public void createsNewTaskWithEmptySubtaskList() {
        Epic epic = new Epic("Test task1", "Test description");
        taskManager.createEpic(epic);
        Epic epicGot = taskManager.getEpicById(1);

        String title = "Test subtask";

        Subtask subtask = new Subtask(title, "Test description", epicGot.getId());
        taskManager.createSubtask(subtask);

        Subtask subtaskGot = taskManager.getSubtaskById(2);

        assertNotNull(
                subtaskGot,
                "После создания подзадачи при пустом менеджере возвращается null вместо подзадачи"
        );

        assertEquals(
                subtask.getTitle(),
                subtaskGot.getTitle(),
                "После создания подзадачи при пустом списке название подзадачи неожиданное"
        );
    }

    @Test
    public void createsNewSubWithIncorrectId() {
        Epic epic = new Epic("Test epic", "Test description");
        taskManager.createEpic(epic);
        Task epicGot = taskManager.getEpicById(1);

        String title = "Test subtask1";
        Subtask subtask = new Subtask(title, "test description", epicGot.getId());
        taskManager.createSubtask(subtask);
        Subtask subtaskGot = taskManager.getSubtaskById(2);

        String title2 = "Test subtask2";
        Subtask subtask2 = new Subtask(title2, "test description", epicGot.getId());
        subtask2.setId(2);
        taskManager.createSubtask(subtask2);
        Subtask subtaskGot2 = taskManager.getSubtaskById(3);

        assertNotNull(
                subtaskGot,
                "При создании подзадачи с некорректным ID первая подзадача не найдена"
        );
        assertNotNull(
                subtaskGot2,
                "При создании подзадачи с некорректным ID вторая подзадача не найдена"
        );

        assertEquals(
                subtaskGot.getTitle(),
                title,
                "При создании подзадачи с некорректным ID у первой подзадачи некорректное название"
        );
        assertEquals(
                subtaskGot2.getTitle(),
                title2,
                "При создании подзадачи с некорректным ID у второй подзадачи некорректное название"
        );
    }

    @Test
    public void updatesSubtask() {
        Epic epic = new Epic("Test epic1", "Test description");
        taskManager.createEpic(epic);

        Epic epicGot = taskManager.getEpicById(1);

        Subtask subtask = new Subtask("Test title", "Test description", epicGot.getId());
        taskManager.createSubtask(subtask);

        Subtask subtaskGot = taskManager.getAllEpicSubtasks(1).get(0);
        subtaskGot.setStatus(Status.DONE);
        taskManager.updateSubtask(subtaskGot);


        assertEquals(
                taskManager.getAllEpicSubtasks(1).get(0).getStatus(),
                Status.DONE,
                "При обновлении статуса подзадачи он не изменилось в менеджере"
        );
    }

    @Test
    public void updatesNonExistingSubtask() {
        Epic epic = new Epic("Test epic", "Test description");
        taskManager.createEpic(epic);

        Epic epicGot = taskManager.getEpicById(1);

        String title = "test title";
        Subtask subtask = new Subtask(title, "test description", epicGot.getId());
        taskManager.createSubtask(subtask);

        Subtask subtask2 = new Subtask("lalala", "test description", epicGot.getId());
        subtask2.setId(3);
        taskManager.updateSubtask(subtask2);

        assertEquals(
                taskManager.getSubtaskById(2).getTitle(),
                title,
                "При обновлении несуществующей подзадачи изменилась существующая"
        );
    }

    @Test
    public void getsAllSubtasks() {
        Epic firstEpic = new Epic("Test epic1", "Test description");
        Epic secondEpic = new Epic("Test epic2", "Test description");
        taskManager.createEpic(firstEpic);
        taskManager.createEpic(secondEpic);

        Subtask firstSubtask = new Subtask("Test subtask1", "Test descr", 1);
        Subtask secondSubtask = new Subtask("Test subtask2", "Test descr", 1);
        Subtask thirdSubtask = new Subtask("Test subtask3", "Test descr", 2);

        Subtask[] subtasks = new Subtask[] {firstSubtask, secondSubtask, thirdSubtask};

        taskManager.createSubtask(firstSubtask);
        taskManager.createSubtask(secondSubtask);
        taskManager.createSubtask(thirdSubtask);


        List<Subtask> subtasksInManager = taskManager.getAllSubtasks();

        for (int i = 0; i < subtasksInManager.size(); i++) {
            assertEquals(
                    subtasksInManager.get(i).getTitle(),
                    subtasks[i].getTitle(),
                    "При обходе всех подзадач из менеджера названия подзадач неожиданные"
            );
        }
    }

    @Test
    public void getsAllSubtasksWithEmptyTaskList() {
        assertTrue(
                taskManager.getAllSubtasks().isEmpty(),
                "При отсутствии подзадач менеджер вернул не пустой список эпиков"
        );
    }

    @Test
    public void deletesAllSubtasks() {
        Epic epic = new Epic("Test epic1", "Test description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test title", "Test descr", 1);
        Subtask subtask2 = new Subtask("Test title", "Test descr", 1);
        Subtask subtask3 = new Subtask("Test title", "Test descr", 1);

        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        int subtaskCountBefore = taskManager.getAllSubtasks().size();

        taskManager.deleteAllSubtasks();

        int subtaskCountAfter = taskManager.getAllSubtasks().size();

        assertNotEquals(
                subtaskCountAfter,
                subtaskCountBefore,
                "При удалении всех подзадач кол-во подзадач до и после совпало"
        );


    }

    @Test
    public void getsSubtaskById() {
        Epic epic = new Epic("Test title", "Test descrition");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("test title", "test descr", 1);
        Subtask subtask2 = new Subtask("test titlefdgh", "test descr", 1);

        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask2);

        Subtask subtaskGot = taskManager.getSubtaskById(2);

        assertNotNull(
                subtaskGot,
                "При попытке получить подзадачу по ID менежер вернул null"
        );
        assertEquals(
                subtask.getTitle(),
                subtaskGot.getTitle(),
                "При попытке получить подзадачу по ID менеджер вернул подзадачу с неожиданным названием"
        );
    }

    @Test
    public void getsNonExistingSubtaskById() {
        Epic epic = new Epic("Test title", "Test descrition");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("test title", "test descr", 1);
        taskManager.createSubtask(subtask);

        Subtask subtaskGot = taskManager.getSubtaskById(3);

        assertNull(
                subtaskGot,
                "При попытке получить несуществующую подазачу по ID менеджер вернул не null"
        );
    }

    @Test
    public void deletesSubtaskById() {
        Epic epic = new Epic("Test title", "Test descrition");
        taskManager.createEpic(epic);


        Subtask subtask = new Subtask("Test title", "Test description", 1);
        taskManager.createSubtask(subtask);

        Subtask subtask2 = new Subtask("Test title", "Test description", 1);
        taskManager.createSubtask(subtask2);

        taskManager.deleteSubtaskById(2);

        Subtask subtaskGot = taskManager.getSubtaskById(2);

        assertNull(
                subtaskGot,
                "При удалении подзадачи по ID подзадача осталась в менеджере"
        );

        assertEquals(
                taskManager.getAllSubtasks().size(),
                1,
                "После удаления подазадачи по ID подзадача не удалилась"
        );
    }

    @Test
    public void deletesNonExistingSubtaskById() {
        Epic epic = new Epic("Test title", "Test description");
        taskManager.createEpic(epic);

        Epic epicGot = taskManager.getEpicById(1);

        Subtask subtask = new Subtask("Test title", "Test description", epicGot.getId());
        taskManager.createSubtask(subtask);

        int subtasksSizeBefore = taskManager.getAllSubtasks().size();

        taskManager.deleteSubtaskById(3);

        int subtasksSizeAfter = taskManager.getAllSubtasks().size();

        assertEquals(
                subtasksSizeBefore,
                subtasksSizeAfter,
                "При удалении несуществующего эпика по id размер списка эпиков в менеджере изменился"
        );
    }
}
