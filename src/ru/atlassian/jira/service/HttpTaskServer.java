package ru.atlassian.jira.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ru.atlassian.jira.exceptions.ManagerInvalidTimePropertiesException;
import ru.atlassian.jira.exceptions.TaskNotFoundException;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.TaskType;
import ru.atlassian.jira.serializers.EpicSerializer;
import ru.atlassian.jira.serializers.SubtaskSerializer;
import ru.atlassian.jira.serializers.TaskSerializer;


public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static Gson gson;
    private final HttpServer server;
    private static final TaskManager manager;

    static {
        try {
            manager = Managers.getHttp("http://localhost:8078");
        } catch (IOException | InterruptedException e) {
            System.out.println("Исключение в менеджере" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public HttpTaskServer() throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskSerializer());
        gsonBuilder.registerTypeAdapter(Epic.class, new EpicSerializer());
        gsonBuilder.registerTypeAdapter(Subtask.class, new SubtaskSerializer());
        gson = gsonBuilder.create();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler());
        server.start();
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

    public void stop(int delay) {
        server.stop(delay);
    }

    static class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            String requestPath = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod();
            String requestQuery = exchange.getRequestURI().getQuery();
            Endpoint endpoint = getEndpoint(
                    requestPath,
                    requestMethod,
                    requestQuery
            );

            try {
                switch (endpoint) {
                    case GET_ALL_TASKS:
                        handleGetAllTasks(exchange);
                        break;
                    case GET_TASK_BY_ID:
                        handleGetTaskById(exchange, requestQuery);
                        break;
                    case POST_TASK:
                        handlePostTask(exchange);
                        break;
                    case DEL_TASK_BY_ID:
                        handleDelTaskById(exchange, requestQuery);
                        break;
                    case DEL_TASKS:
                        handleDelAllTasks(exchange);
                        break;
                    case GET_ALL_EPICS:
                        handleGetAllEpics(exchange);
                        break;
                    case GET_EPIC_BY_ID:
                        handleGetEpicById(exchange, requestQuery);
                        break;
                    case POST_EPIC:
                        handlePostEpic(exchange);
                        break;
                    case DEL_EPIC_BY_ID:
                        handleDelEpicById(exchange, requestQuery);
                        break;
                    case DEL_EPICS:
                        handleDelAllEpics(exchange);
                        break;
                    case GET_ALL_EPIC_SUBTASKS:
                        handleGetAllEpicSubtask(exchange, requestQuery);
                        break;
                    case GET_ALL_SUBTASKS:
                        handleGetAllSubtasks(exchange);
                        break;
                    case GET_SUBTASK_BY_ID:
                        handleGetSubtaskById(exchange, requestQuery);
                        break;
                    case POST_SUBTASK:
                        handlePostSubtask(exchange);
                        break;
                    case DEL_SUBTASK_BY_ID:
                        handleDelSubtaskById(exchange, requestQuery);
                        break;
                    case DEL_SUBTASKS:
                        handleDelAllSubtasks(exchange);
                        break;
                    case GET_HISTORY:
                        handleGetHistory(exchange);
                        break;
                    case GET_PRIORITIZED:
                        handleGetPrioritized(exchange);
                        break;
                    case UNKNOWN:
                        handleUnknown(exchange);
                        break;
                }
            } catch (Exception exception) {
                System.out.println(exception);
            }
        }

        private void handleGetAllTasks(HttpExchange e) throws IOException {
            List<Task> tasks = manager.getAllTasks();
            String responseText = gson.toJson(tasks);
            sendResponse(e, responseText, 200);
        }

        private void handleGetTaskById(HttpExchange e, String query) throws IOException {
            Map<String, String> queryParams = getQueryParams(query);
            String responseText = "";
            int statusCode = 400;
            if (queryParams.get("id") != null) {
                int id = 0;
                Optional<Integer> idFromQuery = getIdFromQueryParams(queryParams.get("id"));
                if (idFromQuery.isPresent()) {
                    id = idFromQuery.get();
                } else {
                    responseText = "Невалидный идентификатор";
                }
                if (id != 0) {
                    Task task = manager.getTaskById(id);
                    if (task == null) {
                        responseText = "Задача с таким идентификатором не найдена";
                        statusCode = 404;
                    } else {
                        responseText = gson.toJson(task);
                        statusCode = 200;
                    }
                }
            } else {
                responseText = "Не передан идентификатор задачи";
            }
            sendResponse(e, responseText, statusCode);
        }

        private void handlePostTask(HttpExchange e) throws IOException {
            int statusCode = 400;
            String responseText;
            InputStream inputStream = e.getRequestBody();
            String requestBody = new String(inputStream.readAllBytes(), UTF8);
            Task inputTask = null;
            try {
                inputTask = gson.fromJson(requestBody, Task.class);
            } catch (JsonSyntaxException exception) {
                responseText = "Невалидный JSON";
            }

            int taskId = inputTask != null ? inputTask.getId() : 0;
            if ((taskId != 0) && (manager.getTaskById(taskId) != null)){
                try {
                    manager.updateTask(inputTask);
                    responseText = "Успешно обновлена задача " + taskId;
                    statusCode = 202;
                } catch (ManagerInvalidTimePropertiesException exception) {
                    responseText = exception.getMessage();
                }
            } else {
                try {
                    manager.createTask(inputTask);
                    responseText = "Успешно создана новая задача";
                    statusCode = 201;
                } catch (ManagerInvalidTimePropertiesException exception) {
                    responseText = exception.getMessage();
                }
            }
            sendResponse(e, responseText, statusCode);
        }

        private void handleDelTaskById(HttpExchange e, String query) throws IOException {
            deleteObject(e, query, TaskType.TASK, "Задача");
        }

        private void handleDelAllTasks(HttpExchange e) throws IOException {
            manager.deleteAllTasks();
            sendResponse(e, "Успешно удалены все задачи", 200);
        }

        private void handleGetAllEpics(HttpExchange e) throws IOException {
            List<Epic> epics = manager.getAllEpics();
            sendResponse(e, gson.toJson(epics), 200);
        }

        private void handleGetEpicById(HttpExchange e, String query) throws IOException {
            Map<String, String> queryParams = getQueryParams(query);
            String responseText = "";
            int statusCode = 400;
            if (queryParams.get("id") != null) {
                int id = 0;
                Optional<Integer> idFromQuery = getIdFromQueryParams(queryParams.get("id"));
                if (idFromQuery.isPresent()) {
                    id = idFromQuery.get();
                } else {
                    responseText = "Невалидный идентификатор";
                }
                if (id != 0) {
                    Epic epic = manager.getEpicById(id);
                    if (epic == null) {
                        responseText = "Эпик с таким идентификатором не найден";
                        statusCode = 404;
                    } else {
                        responseText = gson.toJson(epic);
                        statusCode = 200;
                    }
                }
            } else {
                responseText = "Не передан идентификатор эпика";
            }
            sendResponse(e, responseText, statusCode);
        }

        private void handlePostEpic(HttpExchange e) throws IOException {
            int statusCode = 400;
            String responseText;
            InputStream inputStream = e.getRequestBody();
            String requestBody = new String(inputStream.readAllBytes(), UTF8);
            Map<String, String> inputEpicFields = unpackNonTaskJson(requestBody);
            if ((inputEpicFields.get("title") != null) && (inputEpicFields.get("description") != null)) {
                Epic inputEpic = new Epic(inputEpicFields.get("title"), inputEpicFields.get("description"));
                if (inputEpicFields.get("id") != null) {
                    try {
                        inputEpic.setId(Integer.parseInt(inputEpicFields.get("id")));
                        manager.updateEpic(inputEpic);
                        responseText = "Успешно обновлен эпик " + inputEpicFields.get("id");
                        statusCode = 202;
                    } catch (ManagerInvalidTimePropertiesException exception) {
                        responseText = exception.getMessage();
                    }
                } else {
                    try {
                        manager.createEpic(inputEpic);
                        responseText = "Успешно создан новый эпик";
                        statusCode = 201;
                    } catch (ManagerInvalidTimePropertiesException exception) {
                        responseText = exception.getMessage();
                    }
                }
            } else {
                responseText = "Невалидный набор полей для создания эпика";
            }
            sendResponse(e, responseText, statusCode);
        }

        private void handleDelEpicById(HttpExchange e, String query) throws IOException {
            deleteObject(e, query, TaskType.EPIC, "Эпик");
        }

        private void handleDelAllEpics(HttpExchange e) throws IOException {
            manager.deleteAllEpics();
            sendResponse(e, "Успешно удалены все эпики", 200);
        }

        private void handleGetAllEpicSubtask(HttpExchange e, String query) throws IOException {
            Map<String, String> queryParams = getQueryParams(query);
            String responseText = "";
            int statusCode = 400;
            if (queryParams.get("id") != null) {
                int id = 0;
                Optional<Integer> idFromQuery = getIdFromQueryParams(queryParams.get("id"));
                if (idFromQuery.isPresent()) {
                    id = idFromQuery.get();
                } else {
                    responseText = "Невалидный идентификатор";
                }
                if (id != 0) {
                    Epic epic = manager.getEpicById(id);
                    if (epic == null) {
                        responseText = "Эпик с таким идентификатором не найден";
                        statusCode = 404;
                    } else {
                        List<Subtask> subtasks = manager.getAllEpicSubtasks(id);
                        responseText = gson.toJson(subtasks);
                        statusCode = 200;
                    }
                }
            } else {
                responseText = "Не передан идентификатор эпика";
            }
            sendResponse(e, responseText, statusCode);
        }

        private void handleGetAllSubtasks(HttpExchange e) throws IOException {
            List<Subtask> subtasks = manager.getAllSubtasks();
            sendResponse(e, gson.toJson(subtasks), 200);
        }

        private void handleGetSubtaskById(HttpExchange e, String query) throws IOException {
            Map<String, String> queryParams = getQueryParams(query);
            String responseText = "";
            int statusCode = 400;
            if (queryParams.get("id") != null) {
                int id = 0;
                Optional<Integer> idFromQuery = getIdFromQueryParams(queryParams.get("id"));
                if (idFromQuery.isPresent()) {
                    id = idFromQuery.get();
                } else {
                    responseText = "Невалидный идентификатор";
                }
                if (id != 0) {
                    Subtask subtask = manager.getSubtaskById(id);
                    if (subtask == null) {
                        responseText = "Подзадача с таким идентификатором не найдена";
                        statusCode = 404;
                    } else {
                        responseText = gson.toJson(subtask);
                        statusCode = 200;
                    }
                }
            } else {
                responseText = "Не передан идентификатор эпика";
            }
            sendResponse(e, responseText, statusCode);
        }

        private void handlePostSubtask(HttpExchange e) throws IOException {
            int statusCode = 400;
            String responseText;
            InputStream inputStream = e.getRequestBody();
            String requestBody = new String(inputStream.readAllBytes(), UTF8);
            Map<String, String> inputSubtaskFields = unpackNonTaskJson(requestBody);
            if (
                    (inputSubtaskFields.get("title") != null)
                            && (inputSubtaskFields.get("description") != null)
                            && (inputSubtaskFields.get("epicId") != null)
            ) {
                int epicId = Integer.parseInt(inputSubtaskFields.get("epicId"));
                Subtask inputSubtask = new Subtask(
                        inputSubtaskFields.get("title"),
                        inputSubtaskFields.get("description"),
                        epicId
                );
                if (inputSubtaskFields.get("id") != null) {
                    try {
                        inputSubtask.setId(Integer.parseInt(inputSubtaskFields.get("id")));
                        manager.updateSubtask(inputSubtask);
                        responseText = "Успешно обновлена подзадача " + inputSubtaskFields.get("id");
                        statusCode = 202;
                    } catch (ManagerInvalidTimePropertiesException exception) {
                        responseText = exception.getMessage();
                    }
                } else {
                    try {
                        manager.createSubtask(inputSubtask);
                        Epic epic = manager.getEpicById(epicId);
                        if (epic != null) {
                            epic.addSubtask(inputSubtask);
                        }
                        responseText = "Успешно создана новая подзадача";
                        statusCode = 201;
                    } catch (ManagerInvalidTimePropertiesException | TaskNotFoundException exception) {
                        responseText = exception.getMessage();
                    }
                }
            } else {
                responseText = "Невалидный набор полей для создания эпика";
            }
            sendResponse(e, responseText, statusCode);
        }

        private void handleDelSubtaskById(HttpExchange e, String query) throws IOException {
            deleteObject(e, query, TaskType.SUBTASK, "Подзадача");
        }

        private void handleDelAllSubtasks(HttpExchange e) throws IOException {
            manager.deleteAllSubtasks();
            sendResponse(e, "Успешно удалены все подзадачи", 204);
        }

        private void handleGetHistory(HttpExchange e) throws IOException {
            List<Task> history = manager.getHistory();
            sendResponse(e, gson.toJson(history), 200);
        }

        private void handleGetPrioritized(HttpExchange e) throws IOException {
            List<Task> tasks = manager.getPrioritizedTasks();
            sendResponse(e, gson.toJson(tasks), 200);
        }

        private void handleUnknown(HttpExchange e) throws IOException {
            sendResponse(e, "Подходящий эндпоинт не найден", 404);
        }

        private Map<String, String> unpackNonTaskJson(String requestBody) {
            Map<String, String> fields = new HashMap<>();
            String noBracesBody = requestBody.substring(1, requestBody.length() - 1);
            String[] bodyElems = noBracesBody.split(",");
            for (String bodyElem : bodyElems) {
                String[] entry = bodyElem.split(":");
                String noSpacesKey = entry[0].trim();
                String noSpacesValue = entry[1].trim();
                try {
                    Integer.parseInt(noSpacesValue);
                    fields.put(
                            noSpacesKey.substring(1, noSpacesKey.length() - 1),
                            noSpacesValue
                    );
                } catch (NumberFormatException exception) {
                    if (noSpacesValue.equals("null")) {
                        fields.put(
                                noSpacesKey.substring(1, noSpacesKey.length() - 1),
                                noSpacesValue
                        );
                    } else {
                        fields.put(
                                noSpacesKey.substring(1, noSpacesKey.length() - 1),
                                noSpacesValue.substring(1, noSpacesValue.length() - 1)
                        );
                    }
                }
            }
            return fields;
        }

        private void deleteObject(
                HttpExchange e,
                String query,
                TaskType type,
                String name)
                throws IOException {
            Map<String, String> queryParams = getQueryParams(query);
            String responseText = "";
            int statusCode = 400;
            if (queryParams.get("id") != null) {
                int id = 0;
                Optional<Integer> idFromQuery = getIdFromQueryParams(queryParams.get("id"));
                if (idFromQuery.isPresent()) {
                    id = idFromQuery.get();
                } else {
                    responseText = "Невалидный идентификатор";
                }
                if (id != 0) {
                    switch (type) {
                        case TASK:
                            manager.deleteTaskById(id);
                            break;
                        case EPIC:
                            manager.deleteEpicById(id);
                            break;
                        case SUBTASK:
                            manager.deleteSubtaskById(id);
                            break;
                    }
                    responseText = "Успешно удалено: " + name + "; ID: " + id;
                    statusCode = 200;
                }
            } else {
                responseText = "Не передан идентификатор";
            }
            sendResponse(e, responseText, statusCode);
        }

        private void sendResponse(
                HttpExchange exchange,
                String responseText,
                int statusCode
        ) throws IOException {
            int len = responseText.getBytes(UTF8).length;
            exchange.sendResponseHeaders(statusCode, len);
            if (!responseText.isBlank()) {
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(responseText.getBytes(UTF8));
                }
            }
        }

        private Optional<Integer> getIdFromQueryParams(String queryParam) {
            try {
                 return Optional.of(Integer.parseInt(queryParam));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }

        private Map<String, String> getQueryParams(String query) {
            Map<String, String> queryParams = new HashMap<>();
            if (query != null) {
                for (String queryParam : query.split("&")) {
                    String[] paramKeyValue = queryParam.split("=");
                    if (paramKeyValue.length > 1) {
                        queryParams.put(paramKeyValue[0], paramKeyValue[1]);
                    } else {
                        queryParams.put(paramKeyValue[0], "");
                    }
                }
            }
            return queryParams;
        }

        private Endpoint getEndpoint(String path, String method, String query) {
            Map<String, String> queryParams = getQueryParams(query);

            String[] pathElements = path.split("/");
            if ((pathElements.length == 2) && (pathElements[1].equals("tasks"))) {
                return Endpoint.GET_PRIORITIZED;
            }

            if (pathElements[2].equals("task")) {
                if (!queryParams.getOrDefault("id", "").isEmpty()) {
                    switch (method) {
                        case "GET":
                            return Endpoint.GET_TASK_BY_ID;
                        case "DELETE":
                            return Endpoint.DEL_TASK_BY_ID;
                    }
                } else {
                    switch (method) {
                        case "GET":
                            return Endpoint.GET_ALL_TASKS;
                        case "POST":
                            return Endpoint.POST_TASK;
                        case "DELETE":
                            return Endpoint.DEL_TASKS;
                    }
                }
            }

            if (pathElements[2].equals("epic")) {
                if (!queryParams.getOrDefault("id", "").isEmpty()) {
                    switch (method) {
                        case "GET":
                            return Endpoint.GET_EPIC_BY_ID;
                        case "DELETE":
                            return Endpoint.DEL_EPIC_BY_ID;
                    }
                } else {
                    switch (method) {
                        case "GET":
                            return Endpoint.GET_ALL_EPICS;
                        case "POST":
                            return Endpoint.POST_EPIC;
                        case "DELETE":
                            return Endpoint.DEL_EPICS;
                    }
                }
            }

            if (pathElements[2].equals("subtask")) {
                if (!queryParams.getOrDefault("id", "").isEmpty()) {
                    if ((pathElements.length == 4) && (pathElements[3].equals("epic"))) {
                        return Endpoint.GET_ALL_EPIC_SUBTASKS;
                    }
                    switch (method) {
                        case "GET":
                            return Endpoint.GET_SUBTASK_BY_ID;
                        case "DELETE":
                            return Endpoint.DEL_SUBTASK_BY_ID;
                    }
                } else {
                    switch (method) {
                        case "GET":
                            return Endpoint.GET_ALL_SUBTASKS;
                        case "POST":
                            return Endpoint.POST_SUBTASK;
                        case "DELETE":
                            return Endpoint.DEL_SUBTASKS;
                    }
                }
            }

            if (pathElements[2].equals("history") && method.equals("GET")) {
                return Endpoint.GET_HISTORY;
            }
            return Endpoint.UNKNOWN;
        }

        enum Endpoint {
            GET_ALL_TASKS,
            GET_TASK_BY_ID,
            POST_TASK,
            DEL_TASK_BY_ID,
            DEL_TASKS,
            GET_ALL_EPICS,
            GET_EPIC_BY_ID,
            POST_EPIC,
            DEL_EPIC_BY_ID,
            DEL_EPICS,
            GET_ALL_EPIC_SUBTASKS,
            GET_ALL_SUBTASKS,
            GET_SUBTASK_BY_ID,
            POST_SUBTASK,
            DEL_SUBTASK_BY_ID,
            DEL_SUBTASKS,
            GET_HISTORY,
            GET_PRIORITIZED,
            UNKNOWN
        }
    }
}
