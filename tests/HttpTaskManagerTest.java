
import org.junit.jupiter.api.Assertions;import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import ru.atlassian.jira.exceptions.MessageException;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.service.TaskManager;
import ru.atlassian.jira.service.HttpTaskManager;
import mock.KVTaskClientMock;

public class HttpTaskManagerTest extends TaskManagerTest {
    KVTaskClientMock mockClient;

    @Override
    public TaskManager getProperManager() {
        try {
            mockClient = new KVTaskClientMock("http://localhost:8078");
            HttpTaskManager manager = new HttpTaskManager("http://localhost:8078", mockClient);
            manager.restoreFromSource();
            return manager;
        } catch (IOException | InterruptedException exception) {
            throw new MessageException("Ошибка: " + exception.getMessage());
        }
    }

    @Test
    @DisplayName("Проверка восстановления состояния из хранилища")
    void restoringFromSourceTest() {
        Task newTask = new Task("Новая задача", "Новое опасание", Status.NEW);
        taskManager.createTask(newTask);

        HttpTaskManager secondManager = new HttpTaskManager("http://localhost:8078", mockClient);
        secondManager.restoreFromSource();
        Task taskToTest = secondManager.getTaskById(1);

        Assertions.assertEquals(
                1, secondManager.getAllTasks().size(),
                "После восстановления из моковых данных в менеджере неожиданное кол-во задач"
        );
        Assertions.assertEquals(
                1, taskToTest.getId(),
                "После восстановления из моковых данных в менеджере неожиданное кол-во эпиков"
        );

    }
}
