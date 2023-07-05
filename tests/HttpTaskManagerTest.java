import api.HttpTaskServer;
import api.KVServer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import managers.HttpTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.SingleTask;
import tasks.Subtask;
import tasks.TaskStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTest {
    HttpTaskManager taskManager;
    KVServer kvServer;
    HttpTaskServer taskServer;
    private Gson gson;

    @BeforeEach
    public void start() {
        try {
            taskManager = new HttpTaskManager("http://localhost:" + KVServer.PORT);
            new KVServer().start();
            new HttpTaskServer().start(); // проверить, что этот метод есть в HttpTaskServer!!!!!!
            gson = new Gson();

            taskManager.addTask(new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30, Instant.ofEpochSecond(1687244400)));
            taskManager.addTask(new SingleTask("Задача 2", "Выполнить задачу 2", TaskStatus.NEW, 160, Instant.ofEpochSecond(1687078800)));
            taskManager.addEpic(new Epic("Эпик 1", "Выполнить эпик 1"));
            taskManager.addEpic(new Epic("Эпик 2", "Выполнить эпик 2"));
            taskManager.addSubtask(new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 3, 25, Instant.ofEpochSecond(1687266000)));
            taskManager.addSubtask(new Subtask("Подзадача 2", "Выполнить подзадачу 2", TaskStatus.NEW, 3, 15, Instant.ofEpochSecond(1687273200)));
            taskManager.addSubtask(new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 3, 60, Instant.ofEpochSecond(1687285800)));
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
    public void addTaskTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task");
        String json = gson.toJson(taskManager.getTask(1));
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(gson.toJson(taskManager.getTask(1)), response.body(), "Задачи не идентичны");
    }

    @Test
    public void addEpicTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic");
        String json = gson.toJson(taskManager.getEpic(3));
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(gson.toJson(taskManager.getEpic(3)), response.body(), "Задачи не идентичны");
    }

    @Test
    public void addSubtaskTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        String json = gson.toJson(taskManager.getSubtask(5));
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(gson.toJson(taskManager.getSubtask(5)), response.body(), "Задачи не идентичны");
    }

    // методы updateTaskTest(), updateEpicTest(), updateSubtaskTest() тестируются аналогично методам add

    @Test
    public void getListOfTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Type listType = new TypeToken<ArrayList<SingleTask>>() {
        }
        .getType();
        ArrayList<SingleTask> tasks = gson.fromJson(jsonFile, listType);
        assertNotNull(tasks);
        assertArrayEquals(taskManager.getListOfTasks(), gson.fromJson(response.body());
        assertEquals(2, tasks.size(), "Количества задач в списках не совпадают");

    }
}
