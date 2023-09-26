import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.atlassian.jira.exceptions.ManagerInvalidTimePropertiesException;
import ru.atlassian.jira.service.Managers;
import ru.atlassian.jira.service.TaskManager;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Status;

import java.time.LocalDateTime;
import java.util.List;


public abstract class TaskManagerTest {

    public static TaskManager taskManager;

    public TaskManager getProperManager() {
        return Managers.getDefault();
    }

    @BeforeEach
    public void createManager() {
        taskManager = getProperManager();
    }


    @Test
    public void createsNewTaskWithEmptyTaskList() {
        Task firstTask = new Task("Test task1", "Test description", Status.NEW);
        taskManager.createTask(firstTask);
        Task firstTaskGot = taskManager.getTaskById(1);

        Assertions.assertNotNull(firstTaskGot, "После создания задачи при пустом менеджере возвращается null вместо задачи");
        Assertions.assertEquals(
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

        Assertions.assertEquals(
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

        Assertions.assertNotNull(
                firstTaskGot,
                "При создании задачи с некорректным ID первая задача не найдена"
        );
        Assertions.assertNotNull(
                secondTaskGot,
                "При создании задачи с некорректным ID  вторая задача не найдена"
        );

        Assertions.assertEquals(
                firstTaskGot.getTitle(),
                title,
                "При создании задачи с некорректным ID у первой задачи некорректное название"
        );
        Assertions.assertEquals(
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

        Assertions.assertEquals(
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

        Assertions.assertNotEquals(
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
            Assertions.assertEquals(
                    tasksInManager.get(i).getTitle(),
                    tasks[i].getTitle(),
                    "При обходе всех задач из менеджера названия назад неожиданные"
            );
        }

    }

    @Test
    public void getsAllTasksWithEmptyTaskList() {
        Assertions.assertTrue(
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

        Assertions.assertFalse(
                taskManager.getAllTasks().isEmpty(),
                "При наличии задач менеджер вернул пустой список"
        );

        taskManager.deleteAllTasks();

        Assertions.assertTrue(
                taskManager.getAllTasks().isEmpty(),
                "После удаления всех задач менерджер верну не пустой список"
        );


    }

    @Test
    public void getsTaskById() {
        Task task = new Task("Test title", "Test descrition", Status.NEW);
        taskManager.createTask(task);

        Task taskGot = taskManager.getTaskById(1);

        Assertions.assertNotNull(
                taskGot,
                "При попытке получить задачу по ID менежер вернул null"
        );
        Assertions.assertEquals(
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

        Assertions.assertNull(
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

        Assertions.assertNull(
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

        Assertions.assertEquals(
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

        Assertions.assertEquals(
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

        Assertions.assertNotNull(firstEpicGot, "После создания эпика при пустом менеджере возвращается null вместо эпика");
        Assertions.assertEquals(
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

        Assertions.assertNotNull(
                firstEpicGot,
                "При создании эпика с некорректным ID первый эпик не найден"
        );
        Assertions.assertNotNull(
                secondEpicGot,
                "При создании эпика с некорректным ID  второй эпик не найден"
        );

        Assertions.assertEquals(
                firstEpicGot.getTitle(),
                title,
                "При создании эпика с некорректным ID у первого эпика некорректное название"
        );
        Assertions.assertEquals(
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

        Assertions.assertEquals(
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

        Assertions.assertNotEquals(
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
            Assertions.assertEquals(
                    epicsInManager.get(i).getTitle(),
                    epics[i].getTitle(),
                    "При обходе всех эпиков из менеджера названия эпиков неожиданные"
            );
        }
    }

    @Test
    public void getsAllTasksWithEmptyEpicList() {
        Assertions.assertTrue(
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

        Assertions.assertFalse(
                taskManager.getAllEpics().isEmpty(),
                "При наличии эпиков менеджер вернул пустой список"
        );

        int subtaskCountBefore = taskManager.getAllSubtasks().size();

        taskManager.deleteAllEpics();

        Assertions.assertTrue(
                taskManager.getAllTasks().isEmpty(),
                "После удаления всех эпиков менерджер вернул не пустой список"
        );

        int subtaskCountAfter = taskManager.getAllSubtasks().size();

        Assertions.assertNotEquals(
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

        Assertions.assertNotNull(
                epicGot,
                "При попытке получить эпик по ID менежер вернул null"
        );
        Assertions.assertEquals(
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

        Assertions.assertNull(
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

        Assertions.assertNull(
                epicGot,
                "При удалении эпика по ID эпик осталась в менеджере"
        );

        Assertions.assertEquals(
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

        Assertions.assertEquals(
                epicsSizeBefore,
                epicsSizeAfter,
                "При удалении несуществующего эпика по id размер списка эпиков в менеджере изменился"
        );
        Assertions.assertEquals(
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

        Assertions.assertNotEquals(
                taskManager.getAllEpicSubtasks(epicGot.getId()).size(),
                taskManager.getAllEpicSubtasks(epic2Got.getId()).size(),
                "При получении списков подзадач эпиков размеры большого и малого эпиков совпали"
        );

        Assertions.assertEquals(
                taskManager.getAllEpicSubtasks(epicGot.getId()).get(0).getTitle(),
                title,
                "При получении всех подаздач название первой подзадачи первого эпика не совпало с ожидаемым"
        );

        Assertions.assertEquals(
                taskManager.getAllEpicSubtasks(epic2Got.getId()).get(0).getTitle(),
                title4,
                "При получении всех подзадач название первой подзадачи второго эпика не совпало с ожидаемым"
        );


    }

    @Test
    public void getsAllNonExistingEpicSubtasks() {
        Assertions.assertTrue(
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

        Assertions.assertFalse(
                taskManager.getAllSubtasks().isEmpty(),
                "После создания подзадачи список подзадач пуст"
        );

        Assertions.assertEquals(
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

        Assertions.assertNotNull(
                subtaskGot,
                "После создания подзадачи при пустом менеджере возвращается null вместо подзадачи"
        );

        Assertions.assertEquals(
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

        Assertions.assertNotNull(
                subtaskGot,
                "При создании подзадачи с некорректным ID первая подзадача не найдена"
        );
        Assertions.assertNotNull(
                subtaskGot2,
                "При создании подзадачи с некорректным ID вторая подзадача не найдена"
        );

        Assertions.assertEquals(
                subtaskGot.getTitle(),
                title,
                "При создании подзадачи с некорректным ID у первой подзадачи некорректное название"
        );
        Assertions.assertEquals(
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


        Assertions.assertEquals(
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

        Assertions.assertEquals(
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
            Assertions.assertEquals(
                    subtasksInManager.get(i).getTitle(),
                    subtasks[i].getTitle(),
                    "При обходе всех подзадач из менеджера названия подзадач неожиданные"
            );
        }
    }

    @Test
    public void getsAllSubtasksWithEmptyTaskList() {
        Assertions.assertTrue(
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

        Assertions.assertNotEquals(
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

        Assertions.assertNotNull(
                subtaskGot,
                "При попытке получить подзадачу по ID менежер вернул null"
        );
        Assertions.assertEquals(
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

        Assertions.assertNull(
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

        Assertions.assertNull(
                subtaskGot,
                "При удалении подзадачи по ID подзадача осталась в менеджере"
        );

        Assertions.assertEquals(
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

        Assertions.assertEquals(
                subtasksSizeBefore,
                subtasksSizeAfter,
                "При удалении несуществующего эпика по id размер списка эпиков в менеджере изменился"
        );
    }

    @Test
    public void checksTaskWithTimeCreation() {
        LocalDateTime estimatedStartTime = LocalDateTime.of(2023, 9, 24, 23, 23);
        int estimatedDuration = 10;

        taskManager.createTask(
                new Task(
                        "Title",
                        "Descr",
                        Status.NEW,
                        estimatedDuration,
                        estimatedStartTime
                )
        );

        LocalDateTime estimatedEndTime = LocalDateTime.of(2023, 9, 24, 23, 33);

        Task task = taskManager.getTaskById(1);

        Assertions.assertTrue(
                task.getStartTime().isPresent(),
                "При создании задачи с временными параметрами время старта не вернулось"
        );

        Assertions.assertTrue(
                task.getEndTime().isPresent(),
                "При создании задачи с временными параметрами время завершения не вернулось"
        );

        Assertions.assertEquals(
                estimatedStartTime.compareTo(task.getStartTime().get()),
                0,
                "При создании задачи с временными параметрами время старта не совпало с ожидаемым"
        );


        Assertions.assertEquals(
                estimatedEndTime.compareTo(task.getEndTime().get()),
                0,
                "При создании задачи с временными параметрами время завершения не совпало с ожидаемым"
        );

        Assertions.assertTrue(
                task.getDuration().isPresent(),
                "При создании задачи с временными параметрами длительность не вернулась"
        );

        Assertions.assertEquals(
                task.getDuration().get(),
                estimatedDuration,
                "При создании задачи с временными параметрами длительность не совпала с ожидаемым"
        );

    }

    @Test
    public void checksSubTaskWithTimeCreation() {
        LocalDateTime estimatedStartTime = LocalDateTime.of(2023, 9, 24, 23, 23);
        int estimatedDuration = 10;

        taskManager.createEpic(new Epic("Title", "descr"));

        taskManager.createSubtask(
                new Subtask(
                        "Title",
                        "Descr",
                        1,
                        estimatedDuration,
                        estimatedStartTime
                )
        );

        LocalDateTime estimatedEndTime = LocalDateTime.of(2023, 9, 24, 23, 33);

        Subtask task = taskManager.getSubtaskById(2);

        Assertions.assertTrue(
                task.getStartTime().isPresent(),
                "При создании задачи с временными параметрами время старта не вернулось"
        );

        Assertions.assertTrue(
                task.getEndTime().isPresent(),
                "При создании задачи с временными параметрами время завершения не вернулось"
        );

        Assertions.assertEquals(
                estimatedStartTime.compareTo(task.getStartTime().get()),
                0,
                "При создании задачи с временными параметрами время старта не совпало с ожидаемым"
        );


        Assertions.assertEquals(
                estimatedEndTime.compareTo(task.getEndTime().get()),
                0,
                "При создании задачи с временными параметрами время завершения не совпало с ожидаемым"
        );

        Assertions.assertTrue(
                task.getDuration().isPresent(),
                "При создании задачи с временными параметрами длительность не вернулась"
        );

        Assertions.assertEquals(
                task.getDuration().get(),
                estimatedDuration,
                "При создании задачи с временными параметрами длительность не совпала с ожидаемым"
        );

    }

    @Test
    public void checksEpicTimePropertiesCalculation() {
        LocalDateTime startTime = LocalDateTime.of(2023, 9, 24, 23, 33);
        LocalDateTime startTime2 = LocalDateTime.of(2023, 9, 24, 23, 53);
        int duration = 10;
        taskManager.createEpic(new Epic("fgdfgh", "yfgjf"));
        taskManager.createSubtask(new Subtask("fgdfh", "fhfg", 1, duration, startTime));
        taskManager.createSubtask(new Subtask("fgdfh", "fhfg", 1, duration, startTime2));
        taskManager.createSubtask(new Subtask("fgdfh", "fhfg", 1));

        LocalDateTime estimatedEndTime = LocalDateTime.of(2023, 9, 25, 0, 3);

        Epic epic = taskManager.getEpicById(1);

        Assertions.assertTrue(
                epic.getStartTime().isPresent(),
                "При наполнении эпика задачами время старта эпика не возвращается"
        );

        Assertions.assertTrue(
                epic.getEndTime().isPresent(),
                "При наполнении эпика задачами время завершения эпика не возвращается"
        );

        Assertions.assertTrue(
                epic.getDuration().isPresent(),
                "При наполнении эпика задачами длительность эпика не возвращается"
        );

        Assertions.assertEquals(
                epic.getStartTime().get().compareTo(startTime),
                0,
                "При наполнении эпика задачами дата старта отличается от ожидаемого"

        );

        Assertions.assertEquals(
                epic.getEndTime().get().compareTo(estimatedEndTime),
                0,
                "При наполнении эпика задачами дата завершения отличается от ожидаемого"

        );

        Assertions.assertEquals(
                epic.getDuration().get(),
                20,
                "При наполнении эпика задачами дата завершения отличается от ожидаемого"

        );

    }

    @Test
    public void checksTasksIntersection() {
        LocalDateTime time1 = LocalDateTime.of(2023, 9, 24, 23, 33);
        LocalDateTime time2 = LocalDateTime.of(2023, 9, 24, 23, 43);
        LocalDateTime time3 = LocalDateTime.of(2023, 9, 24, 23, 53);
        Task task1 = new Task("fgsfh", "sdfsgdf", Status.NEW, 15, time1);
        Task task2 = new Task("fgsfh", "sdfsgdf", Status.NEW, 10, time2);
        Task task3 = new Task("fgsfh", "sdfsgdf", Status.NEW, 10, time3);

        taskManager.createTask(task1);

        Assertions.assertThrows(
                ManagerInvalidTimePropertiesException.class,
                () -> taskManager.createTask(task2),
                "Не выбрасывается исключение при попытке создать пересекающиеся задачи"
        );

        taskManager.createTask(task3);

        Assertions.assertNotNull(
                taskManager.getTaskById(2),
                "Не создается задача, проходящая проверку на пересечения"
        );

        Task taskToUpdate = taskManager.getTaskById(1);
        taskToUpdate.setDuration(100);

        Assertions.assertThrows(
                ManagerInvalidTimePropertiesException.class,
                () -> taskManager.updateTask(taskToUpdate),
                "Не выбрасывается исключение при обновлении задачи и конфликте пересечений"
        );

    }

    @Test
    public void checksPrioritizedList() {
        LocalDateTime time1 = LocalDateTime.of(2023, 9, 24, 23, 33);
        LocalDateTime time2 = LocalDateTime.of(2023, 9, 24, 23, 53);
        LocalDateTime time3 = LocalDateTime.of(2023, 9, 24, 23, 13);
        taskManager.createTask(new Task("fgsfh", "sdfsgdf", Status.NEW, 10, time1));
        taskManager.createTask(new Task("fgsfh", "sdfsgdf", Status.NEW, 10, time2));
        taskManager.createTask(new Task("fgsfh", "sdfsgdf", Status.NEW, 10, time3));

        Task task = taskManager.getPrioritizedTasks().get(0);

        Assertions.assertEquals(
                task.getId(),
                3,
                "В списке по приоритетам ID наиболее приоритетной задачи отличается от ожидаемого"
        );

    }

}
