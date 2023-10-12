import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.service.Managers;
import ru.atlassian.jira.service.TaskManager;
import java.util.List;

public class InMemoryHistoryManagerTest {
    public static TaskManager taskManager;

    @BeforeEach
    public void setupForEach() {
        taskManager = Managers.getInMemory();
    }

    @Test
    @DisplayName("Проверяет получение истории")
    void shouldReturnCorrecrHistorySizeAndFirstTaskID() {
        for (int i = 0; i < 5; i++) {
            taskManager.createTask(new Task("4fgd4", "45tfdzc", Status.NEW));
        }
        for (int i = 0; i < 5; i++) {
            taskManager.getTaskById(i + 1);
        }
        List<Task> taskHistory = taskManager.getHistory();

        Assertions.assertEquals(
                5,
                taskHistory.size(),
                "При штатном запросе истории длина списка задач отличается от ожидаемой"
        );

        Assertions.assertEquals(
                1,
                taskHistory.get(0).getId(),
                "При штатном запросе истории ID первой задачи отличается от ожидаемого"
        );
    }

    @Test
    @DisplayName("Проверяет получение пустой истории")
    void shouldTrueWhenHistoryEmpty() {
        Assertions.assertTrue(
                taskManager.getHistory().isEmpty(),
                "При запросе пустой истории мененджер вернул не пустой список"
        );
    }

    @Test
    @DisplayName("Проверяет добавление задачи в историю")
    void shouldFalseEmptyHistoryAndReturnFirstTaskCorrectId() {
        taskManager.createTask(new Task("324fdgf", "456ygfds", Status.DONE));
        taskManager.getTaskById(1);

        List<Task> taskHistory = taskManager.getHistory();

        Assertions.assertFalse(
                taskHistory.isEmpty(),
                "После запроса задачи история пустая"
        );

        Assertions.assertEquals(
                1,
                taskHistory.get(0).getId(),
                "ID задачи в истории после добавления отличается от ожидаемого"
        );
    }

    @Test
    @DisplayName("Проверяет добавление дубликата задачи в историю")
    void shouldReturnHistorySizeEqualsOne() {
        taskManager.createTask(new Task("bhfdjdg", "sdggj3", Status.NEW));
        taskManager.getTaskById(1);
        taskManager.getTaskById(1);

        Assertions.assertEquals(
                1,
                taskManager.getHistory().size(),
                "В историю удалось добавить дубликат задачи"
        );
    }

    @Test
    @DisplayName("Проверяет удаление задачи из начала истории")
    void shouldNotReturnFirstTaskIdAfterItWasHistoryHead() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("fdhfg", "dfghfg", Status.NEW));
        }
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(1);
        List<Task> tasksHistory = taskManager.getHistory();

        Assertions.assertNotEquals(
                1,
                tasksHistory.get(0).getId(),
                "Задача из начала истории не удалена при повторном просмотре"
        );

    }

    @Test
    @DisplayName("Проверяет удаление задачи из конца истории")
    void shourldReturnCorrectHistorySizeAndLastTaskIdAfterTailDeletion() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("fdhfg", "dfghfg", Status.NEW));
        }
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.deleteTaskById(3);
        List<Task> tasksHistory = taskManager.getHistory();

        Assertions.assertEquals(
                2,
                tasksHistory.size(),
                "Длина истории после удаления последней задачи отличается от ожидаемой"
        );

        Assertions.assertEquals(
                2,
                tasksHistory.get(1).getId(),
                 "ID последней задачи в истории после удаления хвоста списка отличается от ожидаемого"
         );
    }

    @Test
    @DisplayName("Проверяет удаление задачи из середины истории")
    void shourldReturnCorrectHistorySizeAndMiddleTaskIdAfterMiddleDeletion() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("fdhfg", "dfghfg", Status.NEW));
        }
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.deleteTaskById(2);
        List<Task> tasksHistory = taskManager.getHistory();

        Assertions.assertEquals(
                2,
                tasksHistory.size(),
                "Длина истории после удаления задачи из середины отличается от ожидаемой"
        );

        Assertions.assertEquals(
                3,
                tasksHistory.get(1).getId(),
                "ID последней задачи в истории после удаления задачи из сережины отличается от ожидаемого"
        );
    }
}
