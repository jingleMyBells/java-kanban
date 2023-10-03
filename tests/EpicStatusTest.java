import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.service.Managers;
import ru.atlassian.jira.service.TaskManager;

class EpicStatusTest {
    public TaskManager taskManager;
    public static Epic epicToTest;

    @BeforeEach
    public void setupForEach() {
        taskManager = Managers.getDefault();
        Epic mainEpic = new Epic("Test main epic", "Test description");
        taskManager.createEpic(mainEpic);
        epicToTest = taskManager.getEpicById(1);
    }

    @Test
    @DisplayName("Проверяет статус пустого эпика")
    void shouldEpicStatusWhenNoSubtaskInEpic() {
            Assertions.assertEquals(
                    Status.NEW,
                    epicToTest.getStatus(),
                    "При отсутствии подзадач тестовый эпик в неожиданном статусе"
            );
    }

    @Test
    @DisplayName("Проверяет статус эпика только с новыми задачами")
    void shouldEpicStatusNewWhenAllSubtasksNew() {
        for (int i = 0; i < 4; i++) {
            taskManager.createSubtask(new Subtask("fhgdfg", "fhgjgh", epicToTest.getId()));
        }

        Assertions.assertEquals(
                Status.NEW,
                epicToTest.getStatus(),
                "При всех новых подзадачах тестовый эпик в неожиданном статусе"
        );
    }

    @Test
    @DisplayName("Проверяет статус эпика только со сделанными задачами")
    void shouldEpicStatusDoneWhenAllSubtasksDone() {
        for (int i = 0; i < 4; i++) {
            taskManager.createSubtask(new Subtask("fhgdfg", "fhgjgh", epicToTest.getId()));
        }

        for (Subtask subTask : taskManager.getAllEpicSubtasks(epicToTest.getId())) {
            subTask.setStatus(Status.DONE);
            taskManager.updateSubtask(subTask);
        }

        Assertions.assertEquals(
                Status.DONE,
                epicToTest.getStatus(),
                "При всех новых подзадачах тестовый эпик в неожиданном статусе"
        );
    }

    @Test
    @DisplayName("Проверяет статус эпика со сделанными и новыми задачами")
    void shouldEpicStatusProgressWhenSubtasksNewOrDone() {
        for (int i = 0; i < 2; i++) {
            taskManager.createSubtask(new Subtask("fhgdfg", "fhgjgh", epicToTest.getId()));
        }

        Subtask subtask2 = taskManager.getAllEpicSubtasks(epicToTest.getId()).get(1);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        Assertions.assertEquals(
                Status.IN_PROGRESS,
                epicToTest.getStatus(),
                "При всех новых либо сделанных подзадачах тестовый эпик в неожиданном статусе"
        );
    }

    @Test
    @DisplayName("Проверяет статус эпика с задачами в статусе IN_PGORESS")
    void shouldEpicStatusProgressWhenSubtasksInProgress() {
        for (int i = 0; i < 4; i++) {
            taskManager.createSubtask(new Subtask("fhgdfg", "fhgjgh", epicToTest.getId()));
        }

        for (Subtask subTask : taskManager.getAllEpicSubtasks(epicToTest.getId())) {
            subTask.setStatus(Status.IN_PROGRESS);
            taskManager.updateSubtask(subTask);
        }

        Assertions.assertEquals(
                Status.IN_PROGRESS,
                epicToTest.getStatus(),
                "При всех новых подзадачах тестовый эпик в неожиданном статусе"
        );
    }
}