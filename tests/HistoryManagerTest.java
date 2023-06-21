import managers.HistoryManager;
import managers.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoryManagerTest {
    HistoryManager historyManager;
    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }
    @Test
    void add() {
        SingleTask task1 = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400));
        task1.setId(1);
        SingleTask task2 = new SingleTask("Задача 2", "Выполнить задачу 2",  TaskStatus.NEW, 160,
                Instant.ofEpochSecond(1687078800));
        task2.setId(2);
        SingleTask task3 = new SingleTask("Задача 3", "Выполнить задачу 3", TaskStatus.NEW, 15,
                Instant.ofEpochSecond(1687284000));
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(3, history.size(), "История не пустая.");

        assertEquals(List.of(task1, task2, task3), historyManager.getHistory(), "История заполнена некорректно");
    }

    @Test
    void remove() {
        SingleTask task = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400));
        task.setId(1);
        historyManager.add(task);
        Epic epic = new Epic("Задача 2", "Выполнить задачу 2");
        epic.setId(2);
        historyManager.add(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 3,
                25, Instant.ofEpochSecond(1687266000));
        subtask1.setId(3);
        Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 3,
                15, Instant.ofEpochSecond(1687273200));
        subtask2.setId(4);
        Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 3,
                60, Instant.ofEpochSecond(1687285800));
        subtask3.setId(5);

        historyManager.add(subtask2);
        historyManager.add(epic);
        historyManager.add(task);
        historyManager.add(subtask2);
        historyManager.add(subtask1);
        historyManager.add(task);
        historyManager.add(subtask3);
        historyManager.add(subtask1);
        historyManager.add(epic);

        assertEquals(List.of(subtask2, task, subtask3, subtask1, epic), historyManager.getHistory(), "История заполнена некорректно");

        historyManager.remove(4); // удаление задачи из начала
        assertEquals(List.of(task, subtask3, subtask1, epic), historyManager.getHistory(), "История заполняется некорректно при удалении задач");

        historyManager.remove(5); // удаление задачи из середины
        assertEquals(List.of(task, subtask1, epic), historyManager.getHistory(), "История заполняется некорректно при удалении задач");

        historyManager.remove(2); // удаление задачи из конца
        assertEquals(List.of(task, subtask1), historyManager.getHistory(), "История заполняется некорректно при удалении задач");
        // удаление подзадач эпика из истории происходит в методе removeEpic() класса InMemoryTaskManager
        // метод remove() удаляет только задачи по заданному id
    }
}