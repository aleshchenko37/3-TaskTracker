import api.HttpTaskServer;
import api.KVServer;
import com.google.gson.*;
import managers.HttpTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTest {
    HttpTaskManager taskManager;
    KVServer kvServer;
    HttpTaskServer taskServer;
    private Gson gson;
    SingleTask task1;
    Epic epic1;
    Subtask subtask1;

    @BeforeEach
    public void start() {
        try {
            kvServer = new KVServer();
            kvServer.start();
            taskManager = new HttpTaskManager("http://localhost:8000");
            taskServer = new HttpTaskServer(taskManager);
            taskServer.start();
            gson = new Gson();

            task1 = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30, Instant.ofEpochSecond(1687244400));
            task1.setId(1);
            epic1 = new Epic("Эпик 1", "Выполнить эпик 1");
            epic1.setId(1);
            subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1, 25, Instant.ofEpochSecond(1687266000));
            subtask1.setId(2);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void end() {
        kvServer.stop();
        taskServer.stop();
    }

    @Test
    public void addTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        String json = gson.toJson(task1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(gson.toJson("Задача успешно добавлена"), response.body());
        assertEquals(1, taskManager.getTasks().size(), "Количество задач не совпадает");
        assertEquals(task1, taskManager.getTask(1), "Задачи не идентичны");
    }

    @Test
    public void getListOfTasks() throws IOException, InterruptedException {
        taskManager.addTask(task1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ArrayList<SingleTask> tasksFromManager = taskManager.getListOfTasks();
        ArrayList<SingleTask> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // 5 - тестовое значение
            if (jsonObject.get(String.valueOf(i)) != null) {
                JsonObject taskJson = jsonObject.get(String.valueOf(i)).getAsJsonObject();
                SingleTask task = gson.fromJson(taskJson, SingleTask.class);
                tasks.add(task);
            }
        }
        assertNotNull(tasks);
        assertEquals(tasksFromManager.size(), tasks.size(), "Количество задач не совпадает");
        assertEquals(tasksFromManager, tasks, "Списки не идентичны");
    }

    @Test
    public void removeTask() throws IOException, InterruptedException {
        taskManager.addTask(task1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1"); // или task/1??
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        ArrayList<SingleTask> tasksFromManager = taskManager.getListOfTasks();
        assertEquals(0, tasksFromManager.size(), "Задача не удалена");
    }

    @Test
    public void removeAllTasks() throws IOException, InterruptedException {
        taskManager.addTask(task1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        ArrayList<SingleTask> tasksFromManager = taskManager.getListOfTasks();
        assertEquals(0, tasksFromManager.size(), "Задача не удалена");
    }

    @Test
    public void getTask() throws IOException, InterruptedException {
        taskManager.addTask(task1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString((String) response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        SingleTask task = gson.fromJson(jsonObject, SingleTask.class);
        assertNotNull(task);
        assertEquals(task1, task, "Задачи не идентичны");
    }

    @Test
    public void addEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        String json = gson.toJson(epic1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(gson.toJson("Эпик успешно добавлен"), response.body());
        assertEquals(1, taskManager.getEpics().size(), "Количество эпиков не совпадает");
        assertEquals(epic1, taskManager.getEpic(1), "Задачи не идентичны");
    }

    @Test
    public void getListOfEpics() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ArrayList<Epic> epicsFromManager = taskManager.getListOfEpics();
        ArrayList<Epic> epics = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // 5 - тестовое значение
            if (jsonObject.get(String.valueOf(i)) != null) {
                JsonObject epicJson = jsonObject.get(String.valueOf(i)).getAsJsonObject();
                Epic epic = gson.fromJson(epicJson, Epic.class);
                epics.add(epic);
            }
        }
        assertNotNull(epics);
        assertEquals(epicsFromManager.size(), epics.size(), "Количество эпиков не совпадает");
        assertEquals(epicsFromManager, epics, "Списки не идентичны");
    }

    @Test
    public void getListOfEpicSubtasks() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        taskManager.addSubtask(subtask1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        ArrayList<Subtask> epicSubtasksFromManager = taskManager.getListOfEpicSubtasks(1);
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i) != null) {
                JsonObject epicSubtasksJson = jsonArray.get(i).getAsJsonObject();
                Subtask subtask = gson.fromJson(epicSubtasksJson, Subtask.class);
                epicSubtasks.add(subtask);
            }
        }
        assertNotNull(epicSubtasks);
        assertEquals(epicSubtasksFromManager.size(), epicSubtasks.size(), "Количество подзадач не совпадает");
        assertEquals(epicSubtasksFromManager, epicSubtasks, "Списки не идентичны");
    }

    @Test
    public void removeEpic() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        ArrayList<Epic> epicsFromManager = taskManager.getListOfEpics();
        assertEquals(0, epicsFromManager.size(), "Задача не удалена");
    }

    @Test
    public void removeAllEpics() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        ArrayList<Epic> epicsFromManager = taskManager.getListOfEpics();
        assertEquals(0, epicsFromManager.size(), "Задача не удалена");
    }

    @Test
    public void getEpic() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString((String) response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Epic epic = gson.fromJson(jsonObject, Epic.class);
        assertNotNull(epic);
        assertEquals(epic1, epic, "Задачи не идентичны");
    }

    @Test
    public void addSubtask() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        String json = gson.toJson(subtask1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(gson.toJson("Подзадача успешно добавлена"), response.body());
        assertEquals(1, taskManager.getSubtasks().size(), "Количество подзадач не совпадает");
        assertEquals(subtask1, taskManager.getSubtask(2), "Задачи не идентичны");
    }

    @Test
    public void getListOfSubtasks() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        taskManager.addSubtask(subtask1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ArrayList<Subtask> subtasksFromManager = taskManager.getListOfSubtasks();
        ArrayList<Subtask> subtasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // 5 - тестовое значение
            if (jsonObject.get(String.valueOf(i)) != null) {
                JsonObject subtaskJson = jsonObject.get(String.valueOf(i)).getAsJsonObject();
                Subtask subtask = gson.fromJson(subtaskJson, Subtask.class);
                subtasks.add(subtask);
            }
        }
        assertNotNull(subtasks);
        assertEquals(subtasksFromManager.size(), subtasks.size(), "Количество подзадач не совпадает");
        assertEquals(subtasksFromManager, subtasks, "Списки не идентичны");
    }

    @Test
    public void removeSubtask() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        taskManager.addSubtask(subtask1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        ArrayList<Subtask> subtasksFromManager = taskManager.getListOfSubtasks();
        assertEquals(0, subtasksFromManager.size(), "Задача не удалена");
    }

    @Test
    public void removeAllSubtasksTest() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        taskManager.addSubtask(subtask1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        ArrayList<Subtask> subtasksFromManager = taskManager.getListOfSubtasks();
        assertEquals(0, subtasksFromManager.size(), "Задача не удалена");
    }

    @Test
    public void getSubtask() throws IOException, InterruptedException {
        taskManager.addEpic(epic1);
        taskManager.addSubtask(subtask1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString((String) response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Subtask subtask = gson.fromJson(jsonObject, Subtask.class);
        assertNotNull(subtask);
        assertEquals(subtask1, subtask, "Задачи не идентичны");
    }

    @Test
    public void getHistory() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString((String) response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Task> historyFromManager = taskManager.getHistory();
        List<Task> history = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i) != null) {
                JsonObject taskJson = jsonArray.get(i).getAsJsonObject();
                Task task = gson.fromJson(taskJson, Task.class);
                history.add(task);
            }
        }
        assertNotNull(history);
        assertEquals(historyFromManager.size(), history.size(), "Количество задач в истории не совпадает");
        assertEquals(historyFromManager, history, "История отличается");
    }

    @Test
    public void getPrioritizedTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString((String) response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Task> prioritizedTasksFromManager = taskManager.getPrioritizedTasks();
        List<Task> prioritizedTasks = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i) != null) {
                JsonObject taskJson = jsonArray.get(i).getAsJsonObject();
                Task task = gson.fromJson(taskJson, Task.class);
                prioritizedTasks.add(task);
            }
        }
        assertNotNull(prioritizedTasks);
        assertEquals(prioritizedTasksFromManager.size(), prioritizedTasks.size(), "Количество задач в истории не совпадает");
        assertEquals(prioritizedTasksFromManager, prioritizedTasks, "История отличается");
    }
}
