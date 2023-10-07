import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.serializers.TaskSerializer;
import ru.atlassian.jira.service.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;


public class HttpTaskServerTest {
    public KVServer kvserver;
    public HttpTaskServer server;
    public final String generalUrl = "http://localhost:8080/tasks";
    public HttpClient client;

    @BeforeEach
    public void startServers() throws IOException {
        kvserver = new KVServer();
        server = new HttpTaskServer();
    }

    @AfterEach
    public void stopKVServer() {
        if (kvserver != null) {
            kvserver.stop();
        }
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    public void createClient() {
        client = HttpClient.newHttpClient();
    }


    @Test
    @DisplayName("Проверяет создание задачи")
    void taskCreation() {}

    @Test
    @DisplayName("Проверяет редактирование задачи")
    void taskUpdate() {}

    @Test
    @DisplayName("Проверяет получение задачи")
    void taskGetting() {}

    @Test
    @DisplayName("Проверяет удаление задачи")
    void taskDeletion() {}

    @Test
    @DisplayName("Проверяет удаление всех задач")
    void allTasksDeletion() {}

    @Test
    @DisplayName("Проверяет создание эпика")
    void epicCreation() {}

    @Test
    @DisplayName("Проверяет редактирование эпика")
    void epicUpdate() {}

    @Test
    @DisplayName("Проверяет получение эпика")
    void epicGetting() {}

    @Test
    @DisplayName("Проверяет удаление эпика")
    void epicDeletion() {}

    @Test
    @DisplayName("Проверяет удаление всех эпиков")
    void allepicDeletion() {}


    @Test
    @DisplayName("Проверяет создание подзадачи")
    void subtaskCreation() {}

    @Test
    @DisplayName("Проверяет редактирование подзадачи")
    void subtaskUpdate() {}

    @Test
    @DisplayName("Проверяет получение подзадачи")
    void subеtaskGetting() {}

    @Test
    @DisplayName("Проверяет удаление подзадачи")
    void subtaskDeletion() {}

    @Test
    @DisplayName("Проверяет удаление всех подзадач")
    void allsubTasksDeletion() {}

    @Test
    @DisplayName("Проверяет получение всех подзадач эпика")
    void epicSubtasks() {}

    @Test
    @DisplayName("Проверяет получение истории")
    void history() {}

    @Test
    @DisplayName("Проверяет получение истории")
    void prioritized() {}


}
