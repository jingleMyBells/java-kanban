package ru.atlassian.jira.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.service.Managers;
import ru.atlassian.jira.service.TaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    public static TaskManager taskManager;
    public static Epic epicToTest;

    @BeforeEach
    public void setupForEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    public void getsHistory() {
        for (int i = 0; i < 5; i++) {
            taskManager.createTask(new Task("4fgd4", "45tfdzc", Status.NEW));
        }
        for (int i = 0; i < 5; i++) {
            taskManager.getTaskById(i + 1);
        }

        List<Task> taskHistory = taskManager.getHistory();

        assertEquals(
                taskHistory.size(),
                5,
                "При штатном запросе истории длина списка задач отличается от ожидаемой"
        );

        assertEquals(
                taskHistory.get(0).getId(),
                1,
                "При штатном запросе истории ID первой задачи отличается от ожидаемого"
        );

    }

    @Test
    public void getsEmptyHistory() {
        assertTrue(
                taskManager.getHistory().isEmpty(),
                "При запросе пустой истории мененджер вернул не пустой список"
        );
    }

    @Test
    public void addsTaskToHistory() {
        taskManager.createTask(new Task("324fdgf", "456ygfds", Status.DONE));
        taskManager.getTaskById(1);

        List<Task> taskHistory = taskManager.getHistory();

        assertFalse(
                taskHistory.isEmpty(),
                "После запроса задачи история пустая"
        );

        assertEquals(
                taskHistory.get(0).getId(),
                1,
                "ID задачи в истории после добавления отличается от ожидаемого"
        );
    }

    @Test
    public void addsTaskDublicate() {
        taskManager.createTask(new Task("bhfdjdg", "sdggj3", Status.NEW));
        taskManager.getTaskById(1);
        taskManager.getTaskById(1);

        assertEquals(
                taskManager.getHistory().size(),
                1,
                "В историю удалось добавить дубликат задачи"
        );
    }

    @Test
    public void removesTaskFromHead() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("fdhfg", "dfghfg", Status.NEW));
        }

        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(1);

        List<Task> tasksHistory = taskManager.getHistory();

        assertNotEquals(
                tasksHistory.get(0).getId(),
                1,
                "Задача из начала истории не удалена при повторном просмотре"
        );

    }

    @Test
    public void removesTaskFromTail() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("fdhfg", "dfghfg", Status.NEW));
        }

        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);

        taskManager.deleteTaskById(3);

        List<Task> tasksHistory = taskManager.getHistory();

        assertEquals(
                tasksHistory.size(),
                2,
                "Длина истории после удаления последней задачи отличается от ожидаемой"
        );

         assertEquals(
                 tasksHistory.get(1).getId(),
                 2,
                 "ID последней задачи в истории после удаления хвоста списка отличается от ожидаемого"
         );

    }

    @Test
    public void removesTaskInBetween() {
        for (int i = 0; i < 3; i++) {
            taskManager.createTask(new Task("fdhfg", "dfghfg", Status.NEW));
        }

        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);

        taskManager.deleteTaskById(2);

        List<Task> tasksHistory = taskManager.getHistory();

        assertEquals(
                tasksHistory.size(),
                2,
                "Длина истории после удаления задачи из середины отличается от ожидаемой"
        );

        assertEquals(
                tasksHistory.get(1).getId(),
                3,
                "ID последней задачи в истории после удаления задачи из сережины отличается от ожидаемого"
        );
    }

}
