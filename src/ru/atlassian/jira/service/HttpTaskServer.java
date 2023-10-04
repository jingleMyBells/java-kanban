package ru.atlassian.jira.service;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;

    public HttpTaskServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler());
        server.start();
        System.out.println("HttpServer Started");
    }

    public void stop(int delay) {
        server.stop(delay);
    }

    static class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Endpoint endpoint = getEndpoint(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getQuery()
            );

            switch (endpoint) {
                case GET_ALL_TASKS:
                    System.out.println("Получение всех задач");
                    break;
                case GET_TASK_BY_ID:
                    System.out.println("Получение задачи по ID");
                    break;
                case POST_TASK:
                    System.out.println("Изменение/создание задачи");
                    break;
                case DEL_TASK_BY_ID:
                    System.out.println("Удаление задачи по айди");
                    break;
                case DEL_TASKS:
                    System.out.println("Удаление всех задач");
                    break;
                case GET_ALL_EPICS:
                    System.out.println("Получение всех эпиков");
                    break;
                case GET_EPIC_BY_ID:
                    System.out.println("Получение эпика по айди");
                    break;
                case POST_EPIC:
                    System.out.println("Создание/изменение эпика");
                    break;
                case DEL_EPIC_BY_ID:
                    System.out.println("Удаление эпика по айди");
                    break;
                case DEL_EPICS:
                    System.out.println("Удаление всех эпиков");
                    break;
                case GET_ALL_EPIC_SUBTASKS:
                    System.out.println("Получение всех подзадач эпика");
                    break;
                case GET_ALL_SUBTASKS:
                    System.out.println("Получение всех подзадач");
                    break;
                case GET_SUBTASK_BY_ID:
                    System.out.println("Получение подзадачи по айди");
                    break;
                case POST_SUBTASK:
                    System.out.println("Создание изменение подзадачи");
                    break;
                case DEL_SUBTASK_BY_ID:
                    System.out.println("Удаление подзадачи по айди");
                    break;
                case DEL_SUBTASKS:
                    System.out.println("Удаление всех подзадач");
                    break;
                case GET_HISTORY:
                    System.out.println("Получение истории");
                    break;
                case GET_PRIORITIZED:
                    System.out.println("Получение всех задач и подзадач по приоритетам");
                    break;
                case UNKNOWN:
                    System.out.println("Неизвестный эндпоинт");
                    break;
            }


        }

        private Endpoint getEndpoint(String path, String method, String query) {
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
