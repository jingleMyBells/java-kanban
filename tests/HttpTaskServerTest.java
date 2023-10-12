import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import ru.atlassian.jira.model.Status;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.serializers.EpicSerializer;
import ru.atlassian.jira.serializers.SubtaskSerializer;
import ru.atlassian.jira.serializers.TaskSerializer;
import ru.atlassian.jira.service.HttpTaskServer;
import ru.atlassian.jira.service.Managers;


public class HttpTaskServerTest {
    public HttpTaskServer server;
    public HttpClient client;
    public String generalUrl = "http://localhost:8080/tasks";

    @BeforeEach
    public void createClientAndServer() {
        try {
            server = new HttpTaskServer(Managers.getInMemory());
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private Gson getGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new HttpTaskServer.LocalDateAdapter());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskSerializer());
        gsonBuilder.registerTypeAdapter(Epic.class, new EpicSerializer());
        gsonBuilder.registerTypeAdapter(Subtask.class, new SubtaskSerializer());
        return gsonBuilder.create();
    }

    private HttpRequest generatePostRequest(String json, URI url) {
        return HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html")
                .header("Content-Type", "application/json")
                .build();
    }

    private HttpRequest generateGetRequest(URI url) {
        return HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html")
                .header("Content-Type", "application/json")
                .build();
    }

    private HttpRequest generateDeleteRequest(URI url) {
        return HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html")
                .build();
    }

    private void createEpicForSubtaskTests() {
        URI url = URI.create(generalUrl + "/epic");
        Epic epic = new Epic("Новая задача", "Новое опасание");
        Gson gson = new Gson();
        HttpRequest postRequest = generatePostRequest(gson.toJson(epic), url);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет создание задачи")
    void shouldReturn201() {
        URI url = URI.create(generalUrl + "/task");
        Task newTask = new Task("Новая задача", "Новое опасание", Status.NEW);
        Gson gson = new Gson();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newTask), url);
        try {
            HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    201,
                    response.statusCode(),
                    "При создании задачи сервер вернул неожиданный статус"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет редактирование задачи")
    void shouldReturn202AndCorrectIdTitleDescrStatus() {
        URI url = URI.create(generalUrl + "/task");
        Task newTask = new Task("Новая задача", "Новое опасание", Status.NEW);
        Task anotherTask = new Task("Другое название", "Другое описание", Status.IN_PROGRESS);
        anotherTask.setId(1);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newTask), url);
        HttpRequest updateRequest = generatePostRequest(gson.toJson(anotherTask), url);
        URI getUrl = URI.create(generalUrl + "/task?=1");
        HttpRequest getRequest = generateGetRequest(getUrl);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    202,
                    updateResponse.statusCode(),
                    "При обновлении задачи сервер вернул неожиданный статус"
            );

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            Type taskListType = new TypeToken<List<Task>>() {}.getType();
            List<Task> tasksFromResponse = gson.fromJson(getResponse.body(), taskListType);
            Task taskToReview = tasksFromResponse.get(0);

            Assertions.assertEquals(
                    1,
                    taskToReview.getId(),
                    "Сервер вернул задачу с неожиданным идентификатором"
            );

            Assertions.assertEquals(
                    "Другое название",
                    taskToReview.getTitle(),
                    "Сервер вернул задачу с неожиданным названием"
            );

            Assertions.assertEquals(
                    "Другое описание",
                    taskToReview.getDescription(),
                    "Сервер вернул задачу с неожиданным описанием"
            );

            Assertions.assertEquals(
                    Status.IN_PROGRESS,
                    taskToReview.getStatus(),
                    "Сервер вернул задачу с неожиданным статусом"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет получение задачи")
    void shouldReturnCorrectSize() {
        URI url = URI.create(generalUrl + "/task");
        Task newTask = new Task("Новая задача", "Новое опасание", Status.NEW);
        Task anotherTask = new Task("Другое название", "Другое описание", Status.IN_PROGRESS);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newTask), url);
        HttpRequest post2Request = generatePostRequest(gson.toJson(anotherTask), url);
        HttpRequest getRequest = generateGetRequest(url);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            client.send(post2Request, HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            Type taskListType = new TypeToken<List<Task>>() {}.getType();
            List<Task> tasksFromResponse = gson.fromJson(getResponse.body(), taskListType);

            Assertions.assertEquals(
                    2,
                    tasksFromResponse.size(),
                    "Сервер вернул список задач неожиданной длины"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет удаление задачи")
    void shouldReturn200AfterDeleteAnd404ForDeletedTask() {
        URI url = URI.create(generalUrl + "/task");
        Task newTask = new Task("Новая задача", "Новое опасание", Status.NEW);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newTask), url);
        URI delUrl = URI.create(generalUrl + "/task?id=1");
        HttpRequest delRequest = generateDeleteRequest(url);
        HttpRequest getRequest = generateGetRequest(delUrl);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> delResponse =  client.send(delRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    200,
                    delResponse.statusCode(),
                    "Сервер вернул неожиданный статус при удалении"
            );

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    404,
                    getResponse.statusCode(),
                    "Сервер вернул неожиданный статус после удаления задачи"
            );

            Assertions.assertEquals(
                    "Задача с таким идентификатором не найдена",
                    getResponse.body(),
                    "Сервер вернул непустой список задач после удаления"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет удаление всех задач")
    void shouldReturn200AndEmptyTaskList() {
        URI url = URI.create(generalUrl + "/task");
        Task newTask = new Task("Новая задача", "Новое опасание", Status.NEW);
        Task anotherTask = new Task("Новая задача", "Новое опасание", Status.NEW);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newTask), url);
        HttpRequest post2Request = generatePostRequest(gson.toJson(anotherTask), url);
        HttpRequest delRequest = generateDeleteRequest(url);
        HttpRequest getRequest = generateGetRequest(url);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            client.send(post2Request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> delResponse = client.send(delRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    200,
                    delResponse.statusCode(),
                    "Сервер вернул неожиданный статус"
            );

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    "[]",
                    getResponse.body(),
                    "Сервер вернул не пустой список после удаления всех задач"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет создание эпика")
    void shouldReturn201Epic() {
        URI url = URI.create(generalUrl + "/epic");
        Epic newEpic = new Epic("Новый эпик", "Новое описание");
        Gson gson = new Gson();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newEpic), url);
        try {
            HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    201,
                    response.statusCode(),
                    "При создании эпика сервер вернул неожиданный статус"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет редактирование эпика")
    void shouldReturn202AndCorrectPropertiesOfEpic() {
        URI url = URI.create(generalUrl + "/epic");
        Epic newEpic = new Epic("Новая задача", "Новое опасание");
        Epic anotherEpic = new Epic("Другое название", "Другое описание");
        anotherEpic.setId(1);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newEpic), url);
        HttpRequest updateRequest = generatePostRequest(gson.toJson(anotherEpic), url);
        URI getUrl = URI.create(generalUrl + "/epic?=1");
        HttpRequest getRequest = generateGetRequest(getUrl);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    202,
                    updateResponse.statusCode(),
                    "При обновлении эпика сервер вернул неожиданный статус"
            );

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            Type epicListType = new TypeToken<List<Epic>>() {}.getType();
            List<Epic> epicsFromResponse = gson.fromJson(getResponse.body(), epicListType);
            Epic epicToReview = epicsFromResponse.get(0);

            Assertions.assertEquals(
                    1,
                    epicToReview.getId(),
                    "Сервер вернул эпик с неожиданным идентификатором"
            );

            Assertions.assertEquals(
                    "Другое название",
                    epicToReview.getTitle(),
                    "Сервер вернул эпик с неожиданным названием"
            );

            Assertions.assertEquals(
                    "Другое описание",
                    epicToReview.getDescription(),
                    "Сервер вернул эпик с неожиданным описанием"
            );
        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет получение эпика")
    void shouldReturnCorrectEpicListSize() {
        URI url = URI.create(generalUrl + "/epic");
        Epic newEpic = new Epic("Новая задача", "Новое опасание");
        Epic anotherEpic = new Epic("Другое название", "Другое описание");
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newEpic), url);
        HttpRequest post2Request = generatePostRequest(gson.toJson(anotherEpic), url);
        HttpRequest getRequest = generateGetRequest(url);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            client.send(post2Request, HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            Type epicListType = new TypeToken<List<Epic>>() {}.getType();
            List<Epic> epicsFromResponse = gson.fromJson(getResponse.body(), epicListType);

            Assertions.assertEquals(
                    2,
                    epicsFromResponse.size(),
                    "Сервер вернул список эпиков неожиданной длины"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет удаление эпика")
    void shouldReturn200OnDeleteAndNotFoundDeletedEpic() {
        URI url = URI.create(generalUrl + "/epic");
        Epic newEpic = new Epic("Новая задача", "Новое опасание");
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newEpic), url);
        URI delUrl = URI.create(generalUrl + "/epic?id=1");
        HttpRequest delRequest = generateDeleteRequest(delUrl);
        HttpRequest getRequest = generateGetRequest(delUrl);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> delResponse =  client.send(delRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    200,
                    delResponse.statusCode(),
                    "Сервер вернул неожиданный статус при удалении"
            );

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    404,
                    getResponse.statusCode(),
                    "Сервер вернул неожиданный статус после удаления эпика"
            );

            Assertions.assertEquals(
                    "Эпик с таким идентификатором не найден",
                    getResponse.body(),
                    "Сервер вернул непустой список эпиков после удаления"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет удаление всех эпиков")
    void shouldReturn200OnDeleteAndEmprtyEpicList() {
        URI url = URI.create(generalUrl + "/epic");
        Epic newEpic = new Epic("Новая задача", "Новое опасание");
        Epic anotherEpic = new Epic("Новая задача", "Новое опасание");
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newEpic), url);
        HttpRequest post2Request = generatePostRequest(gson.toJson(anotherEpic), url);
        HttpRequest delRequest = generateDeleteRequest(url);
        HttpRequest getRequest = generateGetRequest(url);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            client.send(post2Request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> delResponse = client.send(delRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    200,
                    delResponse.statusCode(),
                    "Сервер вернул неожиданный статус"
            );

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    "[]",
                    getResponse.body(),
                    "Сервер вернул не пустой список после удаления всех эпиков"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }


    @Test
    @DisplayName("Проверяет создание подзадачи")
    void shouldReturn201Subtask() {
        createEpicForSubtaskTests();
        URI url = URI.create(generalUrl + "/subtask");
        Subtask newSubtask = new Subtask("Новый эпик", "Новое описание", 1);
        Gson gson = new Gson();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newSubtask), url);
        try {
            HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    201,
                    response.statusCode(),
                    "При создании подзадачи сервер вернул неожиданный статус"
            );
        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет редактирование подзадачи")
    void shouldReturn202AndCorrectSuvtaskProperties() {
        createEpicForSubtaskTests();
        URI url = URI.create(generalUrl + "/subtask");
        Subtask newSubtask = new Subtask("Новая задача", "Новое опасание",1);
        Subtask anotherSubtask = new Subtask("Другое название", "Другое описание", 1);
        anotherSubtask.setId(2);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newSubtask), url);
        HttpRequest updateRequest = generatePostRequest(gson.toJson(anotherSubtask), url);
        URI getUrl = URI.create(generalUrl + "/subtask?=2");
        HttpRequest getRequest = generateGetRequest(getUrl);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    202,
                    updateResponse.statusCode(),
                    "При обновлении подзадачи сервер вернул неожиданный статус"
            );

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            Type subtaskListType = new TypeToken<List<Subtask>>() {}.getType();
            List<Subtask> epicsFromResponse = gson.fromJson(getResponse.body(), subtaskListType);
            Subtask subtaskToReview = epicsFromResponse.get(0);

            Assertions.assertEquals(
                    2,
                    subtaskToReview.getId(),
                    "Сервер вернул подзадачу с неожиданным идентификатором"
            );

            Assertions.assertEquals(
                    "Другое название",
                    subtaskToReview.getTitle(),
                    "Сервер вернул подзадачу с неожиданным названием"
            );

            Assertions.assertEquals(
                    "Другое описание",
                    subtaskToReview.getDescription(),
                    "Сервер вернул подзадачу с неожиданным описанием"
            );
        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет получение подзадачи")
    void souldReturnCorrectSubtaskListSize() {
        createEpicForSubtaskTests();
        URI url = URI.create(generalUrl + "/subtask");
        Subtask newSubtask = new Subtask("Новая задача", "Новое опасание", 1);
        Subtask anotherSubtask = new Subtask("Другое название", "Другое описание", 1);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newSubtask), url);
        HttpRequest post2Request = generatePostRequest(gson.toJson(anotherSubtask), url);
        HttpRequest getRequest = generateGetRequest(url);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            client.send(post2Request, HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            Type subtaskListType = new TypeToken<List<Subtask>>() {}.getType();
            List<Subtask> subtasksFromResponse = gson.fromJson(getResponse.body(), subtaskListType);

            Assertions.assertEquals(
                    2,
                    subtasksFromResponse.size(),
                    "Сервер вернул список подзадач неожиданной длины"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет удаление подзадачи")
    void shoulrdReturn200AfterDeleteAndNotFoundGetSubtask() {
        createEpicForSubtaskTests();
        URI url = URI.create(generalUrl + "/subtask");
        Subtask newSubtask = new Subtask("Новая задача", "Новое опасание", 1);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newSubtask), url);
        URI delUrl = URI.create(generalUrl + "/subtask?id=2");
        HttpRequest delRequest = generateDeleteRequest(delUrl);
        HttpRequest getRequest = generateGetRequest(delUrl);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> delResponse =  client.send(delRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    200,
                    delResponse.statusCode(),
                    "Сервер вернул неожиданный статус при удалении подзадачи"
            );

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    404,
                    getResponse.statusCode(),
                    "Сервер вернул неожиданный статус после удаления подзадачи"
            );

            Assertions.assertEquals(
                    "Подзадача с таким идентификатором не найдена",
                    getResponse.body(),
                    "Сервер вернул непустой список подзадач после удаления"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет удаление всех подзадач")
    void shouldReturn200AfterDeleteAndEmptySubtaskList() {
        createEpicForSubtaskTests();
        URI url = URI.create(generalUrl + "/subtask");
        Subtask newSubtask = new Subtask("Новая задача", "Новое опасание",1);
        Subtask anotherSubtask = new Subtask("Новая задача", "Новое опасание", 1);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newSubtask), url);
        HttpRequest post2Request = generatePostRequest(gson.toJson(anotherSubtask), url);
        HttpRequest delRequest = generateDeleteRequest(url);
        HttpRequest getRequest = generateGetRequest(url);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            client.send(post2Request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> delResponse = client.send(delRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    200,
                    delResponse.statusCode(),
                    "Сервер вернул неожиданный статус"
            );

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(
                    "[]",
                    getResponse.body(),
                    "Сервер вернул не пустой список после удаления всех подзадач"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет получение всех подзадач эпика")
    void shouldReturnOnlyOneCorrectSubtaskIdFromEpicSubtaskList() {
        createEpicForSubtaskTests();
        createEpicForSubtaskTests();
        URI url = URI.create(generalUrl + "/subtask");
        URI urlWithEpic = URI.create(generalUrl + "/subtask/epic?id=1");
        Subtask newSubtask = new Subtask("Новая задача", "Новое опасание",1);
        Subtask anotherSubtask = new Subtask("Новая задача", "Новое опасание", 2);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newSubtask), url);
        HttpRequest post2Request = generatePostRequest(gson.toJson(anotherSubtask), url);
        HttpRequest getRequest = generateGetRequest(urlWithEpic);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            client.send(post2Request, HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            Type subtaskListType = new TypeToken<List<Subtask>>() {}.getType();
            List<Subtask> subtasksFromResponse = gson.fromJson(getResponse.body(), subtaskListType);

            Assertions.assertEquals(
                    1,
                    subtasksFromResponse.size(),
                    "Сервер вернул неожиданное кол-во подзадач"
            );

        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет получение истории")
    void shouldReturnCorrectTaskSizeFromHistoryAndCorrectId() {
        URI url = URI.create(generalUrl + "/task");
        URI urlTask1 = URI.create(generalUrl + "/task?id=1");
        URI urlTask2 = URI.create(generalUrl + "/task?id=2");
        URI historyUrl = URI.create(generalUrl + "/history");
        Task newTask = new Task("Новая задача", "Новое опасание",Status.NEW);
        Task secondTask = new Task("Новая задача", "Новое опасание",Status.NEW);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newTask), url);
        HttpRequest post2Request = generatePostRequest(gson.toJson(secondTask), url);
        HttpRequest getRequest1 = generateGetRequest(urlTask1);
        HttpRequest getRequest2 = generateGetRequest(urlTask2);
        HttpRequest getHistoryRequest = generateGetRequest(historyUrl);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            client.send(post2Request, HttpResponse.BodyHandlers.ofString());
            client.send(getRequest2, HttpResponse.BodyHandlers.ofString());
            client.send(getRequest1, HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> getResponse = client.send(getHistoryRequest, HttpResponse.BodyHandlers.ofString());
            Type taskListType = new TypeToken<List<Task>>() {}.getType();
            List<Task> tasksFromResponse = gson.fromJson(getResponse.body(), taskListType);

            Assertions.assertEquals(
                    2,
                    tasksFromResponse.size(),
                    "Сервер вернул неожиданное кол-во задач в истории"
            );

            Assertions.assertEquals(
                    2,
                    tasksFromResponse.get(0).getId(),
                    "Сервер вернул неожиданный идентификатор задачи в истории"
            );


        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }

    @Test
    @DisplayName("Проверяет получение истории")
    void shouldReturnCorrectFirstPrioritizedTaskId() {
        URI url = URI.create(generalUrl + "/task");
        URI priorTasksUrl = URI.create(generalUrl);
        LocalDateTime time1 = LocalDateTime.of(2023, 10, 11, 1, 23);
        LocalDateTime time2 = LocalDateTime.of(2023, 10, 11, 2, 23);
        Task newTask = new Task("Новая задача", "Новое опасание",Status.NEW, 5, time1);
        Task secondTask = new Task("Новая задача", "Новое опасание",Status.NEW, 5, time2);
        Gson gson = getGsonBuilder();
        HttpRequest postRequest = generatePostRequest(gson.toJson(newTask), url);
        HttpRequest post2Request = generatePostRequest(gson.toJson(secondTask), url);
        HttpRequest getHistoryRequest = generateGetRequest(priorTasksUrl);
        try {
            client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            client.send(post2Request, HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> getResponse = client.send(getHistoryRequest, HttpResponse.BodyHandlers.ofString());
            Type subtaskListType = new TypeToken<List<Task>>() {}.getType();
            List<Task> subtasksFromResponse = gson.fromJson(getResponse.body(), subtaskListType);

            Assertions.assertEquals(
                    1,
                    subtasksFromResponse.get(0).getId(),
                    "Сервер вернул неожиданный идентификатор задачи в истории"
            );
        } catch (IOException | InterruptedException exception) {
            System.out.println("Ошибка при отправке запроса");
        }
    }
}
