import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.service.Managers;
import ru.atlassian.jira.service.TaskManager;

class EpicStatusTest {

    public static TaskManager taskManager;
    public static Epic epicToTest;

    @BeforeEach
    public void setupForEach() {
        taskManager = Managers.getDefault();
        Epic mainEpic = new Epic("Test main epic", "Test description");
        taskManager.createEpic(mainEpic);
        epicToTest = taskManager.getEpicById(1);
    }

    @Test
    public void correctStatusWithNoTasks() {
            Assertions.assertEquals(
                    epicToTest.getStatus(),
                    Status.NEW,
                    "При отсутствии подзадач тестовый эпик в неожиданном статусе"
            );
    }

    @Test
    public void correctStatusWithAllNewTasks() {
        for (int i = 0; i < 4; i++) {
            taskManager.createSubtask(new Subtask("fhgdfg", "fhgjgh", epicToTest.getId()));
        }

        Assertions.assertEquals(
                epicToTest.getStatus(),
                Status.NEW,
                "При всех новых подзадачах тестовый эпик в неожиданном статусе"
        );

    }

    @Test
    public void correctStatusWithAllDoneTasks() {
        for (int i = 0; i < 4; i++) {
            taskManager.createSubtask(new Subtask("fhgdfg", "fhgjgh", epicToTest.getId()));
        }

        for (Subtask subTask : taskManager.getAllEpicSubtasks(epicToTest.getId())) {
            subTask.setStatus(Status.DONE);
            taskManager.updateSubtask(subTask);
        }

        Assertions.assertEquals(
                epicToTest.getStatus(),
                Status.DONE,
                "При всех новых подзадачах тестовый эпик в неожиданном статусе"
        );
    }

    @Test
    public void correctStatusWithNewAndDoneTasks() {
        for (int i = 0; i < 2; i++) {
            taskManager.createSubtask(new Subtask("fhgdfg", "fhgjgh", epicToTest.getId()));
        }

        Subtask subtask2 = taskManager.getAllEpicSubtasks(epicToTest.getId()).get(1);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        Assertions.assertEquals(
                epicToTest.getStatus(),
                Status.IN_PROGRESS,
                "При всех новых либо сделанных подзадачах тестовый эпик в неожиданном статусе"
        );


    }

    @Test
    public void correctStatusWithAllInProgressTasks() {
        for (int i = 0; i < 4; i++) {
            taskManager.createSubtask(new Subtask("fhgdfg", "fhgjgh", epicToTest.getId()));
        }

        for (Subtask subTask : taskManager.getAllEpicSubtasks(epicToTest.getId())) {
            subTask.setStatus(Status.IN_PROGRESS);
            taskManager.updateSubtask(subTask);
        }

        Assertions.assertEquals(
                epicToTest.getStatus(),
                Status.IN_PROGRESS,
                "При всех новых подзадачах тестовый эпик в неожиданном статусе"
        );
    }

}