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

class HttpTaskManagerTest extends TaskManagerTest {
    public KVServer kvserver;
    public HttpTaskServer server;
    public final String generalUrl = "http://localhost:8080/tasks";
    public KVTaskClient kvclient;
    public HttpClient client;



    @Override
    public FileBackedTasksManager getProperManager() {
        try {
            return Managers.getHttp("http://localhost:8078");
        } catch (IOException | InterruptedException exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

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

    @BeforeEach
    public void createKVClient() throws IOException, InterruptedException {
        kvclient = new KVTaskClient("http://localhost:8078");
    }

    @Test
    @DisplayName("Проверяет запись данных в сетевое хранилище")
    void shouldReturnCorrectNotNullIdAfterSavingTask() {
        Gson gson;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskSerializer());
        gson = gsonBuilder.create();
        URI postUrl = URI.create(generalUrl + "/task");
        Task newTask = new Task("Title", "Description", Status.NEW);
        String json = gson.toJson(newTask);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(postUrl).
                POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html")
                .header("Content-Type", "application/json")
                .build();


        try {
            HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    201,
                    postResponse.statusCode(),
                    "Апи при создании задачи ответил с неожиданным статусом"
            );

            String tasksInStorage = kvclient.load("tasks");
            Gson gsonWithNoAdapters = new Gson();
            Type tasksMapType = new TypeToken<Map<Integer, Task>>() {}.getType();
            Map<Integer, Task> tasksFromSource = gsonWithNoAdapters.fromJson(tasksInStorage, tasksMapType);

            Assertions.assertNotNull(tasksFromSource, "Вместо списка задач вернулся null");
            Assertions.assertEquals(
                    1,
                    tasksFromSource.get(1).getId(),
                    "Хранилище вернуло неожиданный идентификатор");
        } catch (IOException | InterruptedException exception) {
            System.out.println("Не получилось выполнить запрос к апи");
        }
    }


    static class LocalDateAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
            jsonWriter.value(localDateTime.format(Task.FORMATTER));
        }

        @Override
        public LocalDateTime read(final JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), Task.FORMATTER);
        }
    }

}
