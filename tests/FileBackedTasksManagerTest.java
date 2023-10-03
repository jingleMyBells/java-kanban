import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.atlassian.jira.exceptions.ManagerReadException;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.service.FileBackedTasksManager;
import ru.atlassian.jira.service.Managers;
import ru.atlassian.jira.service.TaskManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class FileBackedTasksManagerTest extends TaskManagerTest {


    @Override
    public FileBackedTasksManager getProperManager() {
        return Managers.getFileBacked("tasks_test.csv");
    }

    @Test
    @DisplayName("Проверяет создание файла на диске")
    void shouldReturnTitlesInFirstLineAndFirstTaskInSecondLine() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("task" + i, "dfh7y3", Status.NEW));
        }

        taskManager.getTaskById(3);
        taskManager.getTaskById(2);

        File filename = new File("tasks_test.csv");

        Assertions.assertTrue(filename.exists());


        List<String> lines = new ArrayList<>();

        try (FileReader reader = new FileReader(filename, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            while (bufferedReader.ready()) {
                lines.add(bufferedReader.readLine());
            }
        } catch (IOException exception) {
            throw new ManagerReadException("Ошибка чтения задач из файла");
        }

        Assertions.assertEquals(
                "id,type,title,status,description,epicId,duration,startTime",
                lines.get(0),
                "После сохранения задач в файл строка заголовков отличается от ожидаемой"
        );

        Assertions.assertEquals(
                "1,Task,task0,NEW,dfh7y3,0,0",
                lines.get(1),
                "После сохранения задач в файл вторая строка отличается от ожидаемой"
        );
    }

    @Test
    @DisplayName("Проверяет запись истории в файл")
    void shouldReturnTasksIDsInLastLine() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("task" + i, "dfh7y3", Status.NEW));
        }

        taskManager.getTaskById(3);
        taskManager.getTaskById(2);

        File filename = new File("tasks_test.csv");

        Assertions.assertTrue(filename.exists());

        List<String> lines = new ArrayList<>();

        try (FileReader reader = new FileReader(filename, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            while (bufferedReader.ready()) {
                lines.add(bufferedReader.readLine());
            }
        } catch (IOException exception) {
            throw new ManagerReadException("Ошибка чтения задач из файла");
        }

        Assertions.assertEquals(
                "3,2",
                lines.get(lines.size() - 1),
                "После сохранения истории в файла строка с историей отличается от ожидаемой"
        );
    }

    @Test
    @DisplayName("Проверяет восстановление задач из файла")
    void shouldReturnCorrectIDandTitleWithAnotherManagerAndSameFile() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("task" + i, "dfh7y3", Status.NEW));
        }

        taskManager.getTaskById(3);
        taskManager.getTaskById(2);

        TaskManager taskManager2 = Managers.getFileBacked("tasks_test.csv");

        Task task = taskManager2.getTaskById(1);

        Assertions.assertEquals(
                1,
                task.getId(),
                "ID задачи после восстановления из файла отличается от ожидаемого"
        );

        Assertions.assertEquals(
                "task0",
                task.getTitle(),
                "ID задачи после восстановления из файла отличается от ожидаемого"
        );
    }

    @Test
    @DisplayName("Проверяет восстановление автоинкремента идентификаторов из файла")
    void shouldCreateNewTaskInNewManagerWithCorrectID() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("task" + i, "dfh7y3", Status.NEW));
        }

        TaskManager taskManager2 = Managers.getFileBacked("tasks_test.csv");
        taskManager2.createTask(new Task("Newtask", "dfh7y3", Status.NEW));
        List<Task> tasksInNewManager = taskManager2.getAllTasks();

        Assertions.assertEquals(
                4,
                tasksInNewManager.get(tasksInNewManager.size() - 1).getId(),
                "После восстановления автоинкремента ID новой задачи отличается от ожидаемого"
         );
    }

    @AfterEach
    public void deleteFile() {
        Path file = Path.of("tasks_test.csv");
        try {
            Files.delete(file);
        } catch (IOException exception) {
            System.out.println("После тестов не удалось удалить файл");
        }
    }
}
