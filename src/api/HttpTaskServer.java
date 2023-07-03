package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import managers.*;
import tasks.Epic;
import tasks.SingleTask;
import tasks.Subtask;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static api.Endpoint.POST_ADD_TASK;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final HttpServer httpServer;
    private static final Gson gson = new Gson();
    private TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TaskHandler());
        httpServer.start();

        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Endpoint endpoint = getEndpoint(exchange, exchange.getRequestMethod());

            String response;
            //Optional<Integer> taskId = getTaskId(exchange);

            int codeStatus = 200;

            switch (endpoint) {
                    case POST_ADD_TASK:
                        SingleTask task = gson.fromJson(new String(exchange.getRequestBody()
                                .readAllBytes(), StandardCharsets.UTF_8), SingleTask.class);
                        taskManager.addTask(task);
                        response = gson.toJson("Задача успешно добавлена");
                        codeStatus = 201;
                        break;
                    case PUT_UPDATE_TASK:
                        SingleTask newTask = gson.fromJson(new String(exchange.getRequestBody()
                                .readAllBytes(), StandardCharsets.UTF_8), SingleTask.class);
                        taskManager.updateTask(newTask);
                        response = gson.toJson("Задача успешно обновлена");
                        codeStatus = 201;
                        break;
                    case GET_LIST_OF_TASKS:
                        response = gson.toJson(taskManager.getTasks());
                        break;
                    case DELETE_TASK:
                        taskManager.removeTask(getTaskId(exchange));
                        response = "Задача удалена";
                        break;
                    case DELETE_ALL_TASKS:
                        taskManager.removeAllTasks();
                        response = gson.toJson("Все задачи удалены");
                        break;
                    case GET_TASK :
                        if (getTaskId(exchange) != 0) {
                            response = gson.toJson(taskManager.getTask(getTaskId(exchange)));
                        } else {
                            response = "Неверный id задачи";
                            codeStatus = 400;
                        }
                        break;
                    case POST_ADD_EPIC:
                        Epic epic = gson.fromJson(new String(exchange.getRequestBody()
                                .readAllBytes(), StandardCharsets.UTF_8), Epic.class);
                        taskManager.addEpic(epic);
                        response = gson.toJson("Эпик успешно добавлен");
                        codeStatus = 201;
                        break;
                    case PUT_UPDATE_EPIC:
                        Epic newEpic = gson.fromJson(new String(exchange.getRequestBody()
                                .readAllBytes(), StandardCharsets.UTF_8), Epic.class);
                        taskManager.updateEpic(newEpic);
                        response = gson.toJson("Эпик успешно обновлен");
                        codeStatus = 201;
                        break;
                    case GET_LIST_OF_EPICS:
                        response = gson.toJson(taskManager.getEpics());
                        break;
                    case GET_LIST_OF_EPIC_SUBTASKS:
                        response = gson.toJson(taskManager.getListOfEpicSubtasks(getTaskId(exchange)));
                        break;
                    case DELETE_EPIC:
                        taskManager.removeEpic(getTaskId(exchange));
                        response = "Эпик удален";
                        break;
                    case DELETE_ALL_EPICS:
                        taskManager.removeAllEpics();
                        response = gson.toJson("Все эпики удалены");
                        break;
                    case GET_EPIC:
                        if (getTaskId(exchange) != 0) {
                            response = gson.toJson(taskManager.getEpic(getTaskId(exchange)));
                        } else {
                            response = "Неверный id задачи";
                            codeStatus = 400;
                        }
                        break;
                    case POST_ADD_SUBTASK:
                        Subtask subtask = gson.fromJson(new String(exchange.getRequestBody()
                                .readAllBytes(), StandardCharsets.UTF_8), Subtask.class);
                        taskManager.addSubtask(subtask);
                        response = gson.toJson("Подзадача успешно добавлена");
                        codeStatus = 201;
                        break;
                    case PUT_UPDATE_SUBTASK:
                        Subtask newSubtask = gson.fromJson(new String(exchange.getRequestBody()
                                .readAllBytes(), StandardCharsets.UTF_8), Subtask.class);
                        taskManager.updateSubtask(newSubtask);
                        response = gson.toJson("Подзадача успешно обновлена");
                        codeStatus = 201;
                        break;
                    case GET_LIST_OF_SUBTASKS:
                        response = gson.toJson(taskManager.getSubtasks());
                        break;
                    case DELETE_SUBTASK:
                        taskManager.removeSubtask(getTaskId(exchange));
                        response = "Подзадача удалена";
                        break;
                    case DELETE_ALL_SUBTASKS:
                        taskManager.removeAllSubtasks();
                        response = gson.toJson("Все подзадачи удалены");
                        break;
                    case GET_SUBTASK:
                        if (getTaskId(exchange) != 0) {
                            response = gson.toJson(taskManager.getSubtask(getTaskId(exchange)));
                        } else {
                            response = "Неверный id задачи";
                            codeStatus = 400;
                        }
                        break;
                    case GET_HISTORY:
                        response = gson.toJson(taskManager.getHistory());
                        break;
                    case GET_PRIORITIZED_TASKS:
                        response = gson.toJson(taskManager.getPrioritizedTasks());
                        break;
                    default:
                        codeStatus = 400;
                        response = "По этому пути ничего нет";
            }

            exchange.sendResponseHeaders(codeStatus, 0);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(DEFAULT_CHARSET));
            }
        }

        /*private Task getTaskFromRequestBody(HttpExchange exchange) {
            Task task;
            try {
                task = gson.fromJson(new String(exchange.getRequestBody()
                        .readAllBytes(), StandardCharsets.UTF_8), Task.class);
            } catch (IOException e) {
                System.out.println("Некорректный формат задачи");
                task = null;
            }
            return task;
        }*/

        private Integer getTaskId(HttpExchange exchange) {
            // метод получает id задачи и возвращает его
            int taskId;
            if (exchange.getRequestURI().getQuery() != null && exchange.getRequestURI().getQuery().contains("=")) {
                String queryWithTaskId = exchange.getRequestURI().getQuery();
                taskId = Integer.parseInt(queryWithTaskId.substring(queryWithTaskId.indexOf("=") + 1));
            } else {
                taskId = 0;
            }
            return taskId;
        }

        private Endpoint getEndpoint(HttpExchange exchange, String requestMethod) {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            if (requestMethod.equals("GET")) {
                if (pathParts.length == 3) {
                    if (getTaskId(exchange) == 0) { // если id задачи не передан
                        switch (pathParts[2]) {
                            case "task":
                                return Endpoint.GET_LIST_OF_TASKS;
                            case "epic":
                                return Endpoint.GET_LIST_OF_EPICS;
                            case "subtask":
                                return Endpoint.GET_LIST_OF_SUBTASKS;
                            case "history":
                                return Endpoint.GET_HISTORY;
                            case "tasks":
                                return Endpoint.GET_PRIORITIZED_TASKS;
                        }
                    } else { // если id задачи передан
                        switch (pathParts[2]) {
                            case "task":
                                return Endpoint.GET_TASK;
                            case "epic":
                                return Endpoint.GET_EPIC;
                            case "subtask":
                                return Endpoint.GET_SUBTASK;
                        }
                    }
                } else if (pathParts.length == 4 && pathParts[2].equals("subtask") && pathParts[3].equals("epic")) {
                    return Endpoint.GET_LIST_OF_EPIC_SUBTASKS;
                }
            }

            if (requestMethod.equals("POST")) {
                switch (pathParts[2]) {
                    case "task":
                        return POST_ADD_TASK;
                    case "epic":
                        return Endpoint.POST_ADD_EPIC;
                    case "subtask":
                        return Endpoint.POST_ADD_SUBTASK;
                }
            }

            if (requestMethod.equals("PUT")) {
                    switch (pathParts[2]) {
                        case "task":
                            return Endpoint.PUT_UPDATE_TASK;
                        case "epic":
                            return Endpoint.PUT_UPDATE_EPIC;
                        case "subtask":
                            return Endpoint.PUT_UPDATE_SUBTASK;
                    }
            }

            if (requestMethod.equals("DELETE")) {
                if (getTaskId(exchange) == 0) { // если id задачи не передан
                    switch (pathParts[2]) {
                        case "task":
                            return Endpoint.DELETE_ALL_TASKS;
                        case "epic":
                            return Endpoint.DELETE_ALL_EPICS;
                        case "subtask":
                            return Endpoint.DELETE_ALL_SUBTASKS;
                    }
                } else {
                    switch (pathParts[2]) { // если id задачи передан
                        case "task":
                            return Endpoint.DELETE_TASK;
                        case "epic":
                            return Endpoint.DELETE_EPIC;
                        case "subtask":
                            return Endpoint.DELETE_SUBTASK;
                    }
                }
            }
            return Endpoint.NO_SUCH_ENDPOINT;
        }
    }
}
