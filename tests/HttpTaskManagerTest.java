import mock.KVTaskClientMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.atlassian.jira.service.TaskManager;
import ru.atlassian.jira.service.HttpTaskManager;

import java.io.IOException;

public class HttpTaskManagerTest extends TaskManagerTest {

    @Override
    public TaskManager getProperManager() {
        try {
            KVTaskClientMock client = new KVTaskClientMock("http://localhost:8078");
            HttpTaskManager manager = new HttpTaskManager("http://localhost:8078", client);
            manager.restoreFromSource();
            return manager;
        } catch (IOException | InterruptedException exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    @Test
    @DisplayName("Проверка восстановления состояния из хранилища")
    void restoringFromSourceTest() {
        Assertions.assertEquals(
                3, taskManager.getAllTasks().size(),
                "После восстановления из моковых данных в менеджере неожиданное кол-во задач"
                );
        Assertions.assertEquals(
                3, taskManager.getAllEpics().size(),
                "После восстановления из моковых данных в менеджере неожиданное кол-во эпиков"
        );
    }
}
