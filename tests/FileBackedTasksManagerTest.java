import exceptions.ManagerSaveException;
import managers.FileBackedTasksManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tasks.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    @TempDir
    public Path tempDir; // создается временная директория, так как поле помечено @TempDir
    Path historyPath;
    String historyFile = "historyFileTest.csv";

    @Test
    public void testFileHolder() {
        assertNotNull(tempDir);
    }

    @BeforeEach
    public void setUp() throws IOException {
        //метод создает и записывает файл с именем historyFileTest.csv во временный каталог tempDir
        try {
            historyPath = tempDir.resolve(historyFile);
            // resolve() из абсолютного (tempDir) и относительного (historyPath) пути строит новый абсолютный путь к новому файлу с историей
            // после завершения тестов файл удаляется

        }
        catch(InvalidPathException ipe) {
            System.err.println(
                    "error creating temporary test file in " +
                            this.getClass().getSimpleName() );
        }
        taskManager = new FileBackedTasksManager(historyPath.toString()); // создается новый менеджер без истории
    }

    @Test
    void loadFromFile() {
        try {
            taskManager = FileBackedTasksManager.loadFromFile(new File("C:\\Users\\alesh\\dev\\java-kanban\\src\\historyFile.csv"));
        } catch (IOException e) {
            e.getMessage();
        }
        assertNotNull(taskManager.getTasks(), "Задачи не добавлены");
        assertNotNull(taskManager.getEpics(), "Эпики не добавлены");
        assertNotNull(taskManager.getSubtasks(), "Подзадачи не добавлены");
        assertNotNull(taskManager.getHistory(), "История не добавлена");


        assertEquals(2, taskManager.getTasks().size(), "Количество задач не совпадает.");
        assertEquals(2, taskManager.getEpics().size(), "Количество эпиков не совпадает.");
        assertEquals(3, taskManager.getSubtasks().size(), "Количество подзадач не совпадает.");
        assertEquals(7, taskManager.getHistory().size(), "Количество задач в истории некорректно");

        SingleTask task1 = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400));
        task1.setId(1);
        SingleTask task2 = new SingleTask("Задача 2", "Выполнить задачу 2",  TaskStatus.NEW, 160,
                Instant.ofEpochSecond(1687078800));
        task2.setId(2);
        Epic epic1 = new Epic("Эпик 1", "Выполнить эпик 1");
        epic1.setId(3);
        Epic epic2 = new Epic("Эпик 2", "Выполнить эпик 2");
        epic2.setId(4);
        Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 3,
                25, Instant.ofEpochSecond(1687266000));
        subtask1.setId(5);
        Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 3,
                15, Instant.ofEpochSecond(1687273200));
        subtask2.setId(6);
        Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 3,
                60, Instant.ofEpochSecond(1687285800));
        subtask3.setId(7);

        List<SingleTask> listOfTasks = List.of(task1, task2);
        List<Epic> listOfEpics = List.of(epic1, epic2);
        List<Subtask> listOfSubtasks = List.of(subtask1, subtask2, subtask3);
        List<Task> history = List.of(epic2, task1, task2, epic1, subtask3, subtask2, subtask1);

        assertEquals(listOfTasks, taskManager.getListOfTasks(), "Задачи загружены некорректно");
        assertEquals(listOfEpics, taskManager.getListOfEpics(), "Эпики загружены некорректно");
        assertEquals(listOfSubtasks, taskManager.getListOfSubtasks(), "Подзадачи загружены некорректно");
        assertEquals(history, taskManager.getHistory(), "История загружена некорректно");
    }
    @Test
    void save() {
        taskManager.addTask(new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400))); // 20/06/ 07:00
        taskManager.addTask(new SingleTask("Задача 2", "Выполнить задачу 2",  TaskStatus.NEW, 160,
                Instant.ofEpochSecond(1687078800))); // 18/06/ 09:00
        taskManager.addEpic(new Epic("Эпик 1", "Выполнить эпик 1"));
        taskManager.addSubtask(new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 3,
                25, Instant.ofEpochSecond(1687266000))); //20/06 13:00
        taskManager.addSubtask(new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 3,
                15, Instant.ofEpochSecond(1687273200))); //20/06 15:00
        taskManager.addSubtask(new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 3,
                60, Instant.ofEpochSecond(1687285800))); //20/06 18:30

        taskManager.getEpic(3);
        taskManager.getTask(1);
        taskManager.getSubtask(6);
        taskManager.getTask(2);
        taskManager.getEpic(3);
        taskManager.getSubtask(4);
        taskManager.getSubtask(6);
        taskManager.getSubtask(5);
        taskManager.removeSubtask(5);

        List<String> tasksArray = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(historyPath.toString(), StandardCharsets.UTF_8))) {
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (!line.equals("")) {
                    tasksArray.add(line);
                }
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка чтения файла");
        }
        List<String> savedManager = List.of(
                "id,type,name,status,description,epic,duration,startTime",
                "1,TASK,Задача 1,NEW,Выполнить задачу 1,30,2023-06-20T07:00:00Z",
                "2,TASK,Задача 2,NEW,Выполнить задачу 2,160,2023-06-18T09:00:00Z",
                "3,EPIC,Эпик 1,NEW,Выполнить эпик 1,0,2023-06-20T13:00:00Z",
                "4,SUBTASK,Подзадача 1,NEW,Выполнить подзадачу 1,3,25,2023-06-20T13:00:00Z",
                "6,SUBTASK,Подзадача 3,NEW,Выполнить подзадачу 3,3,60,2023-06-20T18:30:00Z",
                "1,2,3,4,6,"
        );
        assertEquals(savedManager, tasksArray, "Ошибка сохранения задач");
    }
}
