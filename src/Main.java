import api.KVServer;
import managers.HttpTaskManager;
import managers.Managers;
import tasks.Epic;
import tasks.SingleTask;
import tasks.Subtask;
import tasks.TaskStatus;

import java.io.IOException;
import java.time.Instant;


public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        KVServer kvServer = new KVServer();
        kvServer.start();
        HttpTaskManager taskManager = Managers.getDefault(); // в конструкторе менеджера создается и открывается KVServer
        taskManager.addTask(new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400))); // 20/06/ 07:00
        taskManager.addTask(new SingleTask("Задача 2", "Выполнить задачу 2", TaskStatus.NEW, 160,
                Instant.ofEpochSecond(1687078800))); // 18/06/ 09:00
        taskManager.addEpic(new Epic("Эпик 1", "Выполнить эпик 1"));
        taskManager.addEpic(new Epic("Эпик 2", "Выполнить эпик 2"));
        taskManager.addSubtask(new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 3,
                25, Instant.ofEpochSecond(1687266000))); //20/06 13:00
        taskManager.addSubtask(new Subtask("Подзадача 2", "Выполнить подзадачу 2", TaskStatus.NEW, 3,
                15, Instant.ofEpochSecond(1687273200))); //20/06 15:00
        taskManager.addSubtask(new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 3,
                60, Instant.ofEpochSecond(1687285800))); //20/06 18:30

        taskManager.getEpic(4);
        taskManager.getEpic(3);
        taskManager.getTask(1);
        taskManager.getSubtask(6);
        taskManager.getTask(2);
        taskManager.getEpic(3);
        taskManager.getSubtask(7);
        taskManager.getSubtask(6);
        taskManager.getSubtask(5);

        System.out.println(taskManager.getHistory());
        System.out.println(taskManager.getPrioritizedTasks());


        HttpTaskManager newHttpTaskManager = taskManager.load(taskManager.getKvTaskClient().getAPI_TOKEN());
        System.out.println(newHttpTaskManager.getHistory());
        System.out.println(newHttpTaskManager.getPrioritizedTasks());

        kvServer.stop();
    }
}
