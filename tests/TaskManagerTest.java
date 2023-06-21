import exceptions.SameTimeException;
import exceptions.TaskNotFoundException;
import managers.TaskManager;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    // тесты для проверки методов работы с SingleTask
    @Test
    void addTask() {
        SingleTask task = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400)); // 20/06/ 07:00
        taskManager.addTask(task);

        final SingleTask savedTask = taskManager.getTask(1);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<SingleTask> tasks = taskManager.getListOfTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void updateTask() {
        final SingleTask task = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400)); // 20/06/ 07:00
        taskManager.addTask(task);
        final SingleTask newTask = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.IN_PROGRESS, 30,
                Instant.ofEpochSecond(1687255200)); // 20/06/ 10:00
        newTask.setId(1); // устанавливаем задаче id старой задачи
        taskManager.updateTask(newTask);
        final SingleTask updatedTask = taskManager.getTask(1);

        assertNotNull(newTask, "Задача не найдена.");
        assertEquals(newTask, updatedTask, "Задачи не совпадают.");

        final HashMap<Integer, SingleTask> tasks = taskManager.getTasks();

        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(newTask, tasks.get(1), "Задачи не совпадают.");
    }

    @Test
    void getListOfTasks() {
        final SingleTask singleTask = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400));
        taskManager.addTask(singleTask); // присвоен id 1
        List<SingleTask> tasksList = new ArrayList<>(List.of(singleTask));
        List<SingleTask> tasksListFromManager = taskManager.getListOfTasks();

        assertNotNull(tasksListFromManager, "Список задач не найден.");
        assertEquals(tasksList, tasksListFromManager, "Списки задач не совпадают.");
    }

    @Test
    void removeTask() {
        taskManager.removeTask(1); // проверка удаления задачи из пустого списка
        assertEquals(0, taskManager.getTasks().size());

        final SingleTask singleTask = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400));
        taskManager.addTask(singleTask); // присвоен id 1
        int taskId = singleTask.getId();

        taskManager.removeTask(999); // проверка удаления задачи с несуществующим id
        assertEquals(1, taskManager.getTasks().size());

        taskManager.removeTask(taskId); // проверка удаления задачи по id
        assertNull(taskManager.getTasks().get(taskId), "Задача удалена.");
    }

    @Test
    void removeAllTasks() {
        taskManager.removeAllTasks(); // проверка удаления задач из пустого списка
        assertEquals(0, taskManager.getTasks().size());

        final SingleTask singleTask1 = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400));
        final SingleTask singleTask2 = new SingleTask("Задача 2", "Выполнить задачу 2",  TaskStatus.NEW, 160,
                Instant.ofEpochSecond(1687078800));
        taskManager.addTask(singleTask1); // присвоен id 1
        taskManager.addTask(singleTask2); // присвоен id 2
        taskManager.removeAllTasks(); // проверка удаления задач
        assertEquals(0, taskManager.getTasks().size());
    }

    @Test
    void getTask() {
        final TaskNotFoundException exception = assertThrows( // проверка получения задачи из пустого списка
                TaskNotFoundException.class,
                () -> taskManager.getTask(1)
        );

        assertEquals("Задача не найдена", exception.getMessage());

        final SingleTask singleTask = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400));
        taskManager.addTask(singleTask); // присвоен id 1
        assertEquals(singleTask, taskManager.getTask(1), "Задачи не совпадают."); // проверка получения задачи по id
    }

    // тесты для проверки методов работы с Epic
    @Test
    void addEpic() {
        Epic epic = new Epic("Задача 1", "Выполнить задачу 1");
        taskManager.addEpic(epic);

        final Epic savedEpic = taskManager.getEpic(1);

        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic, savedEpic, "Задачи не совпадают.");

        final List<Epic> epics = taskManager.getListOfEpics();

        assertNotNull(epics, "Задачи на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.get(0), "Задачи не совпадают.");

        TaskStatus epicStatus = epic.getStatus();
        assertEquals(TaskStatus.NEW, epicStatus, "Статус эпика присваивается неверно");
    }

    @Test
    void updateEpic() {
        final Epic epic = new Epic("Задача 1", "Выполнить задачу 1");
        taskManager.addEpic(epic); // присвоен id 1
        final Epic newEpic = new Epic("Задача 1", "Хорошо выполнить задачу 1");
        newEpic.setId(1); // устанавливаем задаче id старой задачи
        taskManager.updateEpic(newEpic);
        final Epic updatedEpic = taskManager.getEpic(1);

        assertNotNull(newEpic, "Задача не найдена.");
        assertEquals(newEpic, updatedEpic, "Задачи не совпадают.");

        final HashMap<Integer, Epic> epics = taskManager.getEpics();

        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(newEpic, epics.get(1), "Задачи не совпадают.");
    }

    @Test
    void getListOfEpics() {
        final Epic epic = new Epic("Задача 1", "Выполнить задачу 1");
        taskManager.addEpic(epic); // присвоен id 1
        List<Epic> epicsList = new ArrayList<>(List.of(epic));
        List<Epic> epicsListFromManager = taskManager.getListOfEpics();

        assertNotNull(epicsListFromManager, "Список задач не найден.");
        assertEquals(epicsList, epicsListFromManager, "Списки задач не совпадают.");
    }

    @Test
    void getListOfEpicSubtasks() {
        final TaskNotFoundException exception = assertThrows( // проверка получения эпика из пустого списка
                TaskNotFoundException.class,
                () -> taskManager.getListOfEpicSubtasks(1)
        );

        assertEquals("Эпик не найден", exception.getMessage());

        final Epic epic = new Epic("Задача 1", "Выполнить задачу 1");
        taskManager.addEpic(epic); // присвоен id 1
        final Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                25, Instant.ofEpochSecond(1687266000));
        final Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 1,
                15, Instant.ofEpochSecond(1687273200));
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        List<Subtask> subtasksOfEpic = new ArrayList<>(List.of(subtask1, subtask2));
        List<Subtask> subtasksListFromManager = taskManager.getListOfEpicSubtasks(1);

        assertNotNull(subtasksListFromManager, "Список задач не найден.");
        assertEquals(subtasksOfEpic, subtasksListFromManager, "Списки задач не совпадают.");
    }

    @Test
    void removeEpic() {
        taskManager.removeEpic(1); // проверка удаления задачи из пустого списка
        assertEquals(0, taskManager.getEpics().size());

        final Epic epic = new Epic("Задача 1", "Выполнить задачу 1");
        taskManager.addEpic(epic); // присвоен id 1
        int epicId = epic.getId();

        taskManager.removeEpic(999); // проверка удаления задачи с несуществующим id
        assertEquals(1, taskManager.getEpics().size());

        taskManager.removeEpic(epicId); // проверка удаления задачи по id
        assertNull(taskManager.getEpics().get(epicId), "Задача удалена.");
    }

    @Test
    void removeAllEpics() {
        taskManager.removeAllEpics(); // проверка удаления задач из пустого списка
        assertEquals(0, taskManager.getEpics().size());

        final Epic epic1 = new Epic("Задача 1", "Выполнить задачу 1");
        final Epic epic2 = new Epic("Задача 2", "Выполнить задачу 2");
        final Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                25, Instant.ofEpochSecond(1687266000));
        final Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 1,
                15, Instant.ofEpochSecond(1687273200));
        final Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 1,
                60, Instant.ofEpochSecond(1687285800));
        final Subtask subtask4 = new Subtask("Подзадача 4", "Выполнить подзадачу 4", TaskStatus.NEW, 1,
                120, Instant.ofEpochSecond(1687284000));
        taskManager.addEpic(epic1); // присвоен id 1
        taskManager.addEpic(epic2); // присвоен id 2
        taskManager.addSubtask(subtask1); // присвоен id 3
        taskManager.addSubtask(subtask1); // присвоен id 4
        taskManager.addSubtask(subtask1); // присвоен id 5
        taskManager.addSubtask(subtask1); // присвоен id 6

        taskManager.removeAllEpics(); // проверка удаления задач
        assertEquals(0, taskManager.getEpics().size());
        assertEquals(0, taskManager.getSubtasks().size());
    }

    @Test
    void getEpic() {
        final TaskNotFoundException exception = assertThrows( // проверка получения задачи из пустого списка
                TaskNotFoundException.class,
                () -> taskManager.getEpic(1)
        );

        assertEquals("Задача не найдена", exception.getMessage());

        final Epic epic = new Epic("Задача 1", "Выполнить задачу 1");
        taskManager.addEpic(epic); // присвоен id 1
        assertEquals(epic, taskManager.getEpic(1), "Задачи не совпадают."); // проверка получения задачи по id
    }

    // тесты для проверки методов работы с Subtask
    @Test
    void addSubtask() {
        Epic epic = new Epic("Эпик 1", "Выполнить эпик 1");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                25, Instant.ofEpochSecond(1687266000));
        taskManager.addSubtask(subtask);

        final Subtask savedSubtask = taskManager.getSubtask(subtask.getId());

        assertNotNull(savedSubtask, "Задача не найдена.");
        assertEquals(subtask, savedSubtask, "Задачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getListOfSubtasks();

        assertNotNull(subtasks, "Задачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask, subtasks.get(0), "Задачи не совпадают.");

        Subtask subtaskWithWrongEpicId = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 5,
                25, Instant.ofEpochSecond(1687266000));

        final TaskNotFoundException exception = assertThrows( // проверка переданной подзадачи на null и корректности id эпика
                TaskNotFoundException.class,
                () -> taskManager.addSubtask(subtaskWithWrongEpicId)
        );
        assertEquals("Передан неверный id эпика", exception.getMessage());
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Эпик 1", "Выполнить эпик 1");
        taskManager.addEpic(epic); // присвоен id 1
        Subtask subtask = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                25, Instant.ofEpochSecond(1687266000));
        taskManager.addSubtask(subtask); // присвоен id 2
        final Subtask newSubtask = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.IN_PROGRESS, 1,
                30, Instant.ofEpochSecond(1687266000));
        newSubtask.setId(2); // устанавливаем задаче id старой задачи
        taskManager.updateSubtask(newSubtask);
        final Subtask updatedSubtask = taskManager.getSubtask(2);

        assertNotNull(newSubtask, "Задача не найдена.");
        assertEquals(newSubtask, updatedSubtask, "Задачи не совпадают.");

        final HashMap<Integer, Subtask> subtasks = taskManager.getSubtasks();

        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(newSubtask, subtasks.get(2), "Задачи не совпадают.");
    }

    @Test
    void getListOfSubtasks() {
        Epic epic = new Epic("Эпик 1", "Выполнить эпик 1");
        taskManager.addEpic(epic); // присвоен id 1
        Subtask subtask = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                30, Instant.ofEpochSecond(1687266000));
        taskManager.addSubtask(subtask); // присвоен id 2
        List<Subtask> subtasksList = new ArrayList<>(List.of(subtask));
        List<Subtask> subtasksListFromManager = taskManager.getListOfSubtasks();

        assertNotNull(subtasksListFromManager, "Список задач не найден.");
        assertEquals(subtasksList, subtasksListFromManager, "Списки задач не совпадают.");
    }

    @Test
    void removeSubtask() {
        taskManager.removeSubtask(1); // проверка удаления задачи из пустого списка
        assertEquals(0, taskManager.getSubtasks().size());

        Epic epic = new Epic("Эпик 1", "Выполнить эпик 1");
        taskManager.addEpic(epic); // присвоен id 1
        Subtask subtask = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.IN_PROGRESS, 1,
                30, Instant.ofEpochSecond(1687266000));
        taskManager.addSubtask(subtask); // присвоен id 2
        int subtaskId = subtask.getId();

        taskManager.removeSubtask(999); // проверка удаления задачи с несуществующим id
        assertEquals(1, taskManager.getSubtasks().size());

        taskManager.removeSubtask(subtaskId); // проверка удаления задачи по id
        assertNull(taskManager.getSubtasks().get(subtaskId), "Задача удалена.");
    }

    @Test
    void removeAllSubtasks() {
        taskManager.removeAllSubtasks(); // проверка удаления задач из пустого списка
        assertEquals(0, taskManager.getTasks().size());

        Epic epic = new Epic("Эпик 1", "Выполнить эпик 1");
        taskManager.addEpic(epic); // присвоен id 1
        final Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                25, Instant.ofEpochSecond(1687266000));
        final Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 1,
                15, Instant.ofEpochSecond(1687273200));
        final Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 1,
                60, Instant.ofEpochSecond(1687285800));
        taskManager.addSubtask(subtask1); // присвоен id 2
        taskManager.addSubtask(subtask2); // присвоен id 3
        taskManager.addSubtask(subtask3); // присвоен id 4

        taskManager.removeAllSubtasks(); // проверка удаления задач
        assertEquals(0, taskManager.getSubtasks().size());

        boolean isEmpty = epic.getSubtasksIds().isEmpty();
        assertEquals(true, isEmpty, "Подзадачи не удалены");
    }

    @Test
    void getSubtask() {
        final TaskNotFoundException exception = assertThrows( // проверка получения задачи из пустого списка
                TaskNotFoundException.class,
                () -> taskManager.getSubtask(1)
        );

        assertEquals("Задача не найдена", exception.getMessage());

        Epic epic = new Epic("Эпик 1", "Выполнить эпик 1");
        taskManager.addEpic(epic); // присвоен id 1
        Subtask subtask = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                25, Instant.ofEpochSecond(1687266000));
        taskManager.addSubtask(subtask); // присвоен id 2
        assertEquals(subtask, taskManager.getSubtask(2), "Задачи не совпадают."); // проверка получения задачи по id
    }

    @Test
    void checkIfSubtaskHasEpicId() {
        Epic epic = new Epic("Эпик 1", "Выполнить эпик 1");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                25, Instant.ofEpochSecond(1687266000));
        taskManager.addSubtask(subtask);
        int epicId = taskManager.getSubtask(2).getEpicId();
        assertEquals(1, epicId, "Поле epicId не заполнено");
    }

    @Test
    void getHistory() {
        SingleTask singleTask1 = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400));
        SingleTask singleTask2 = new SingleTask("Задача 2", "Выполнить задачу 2",  TaskStatus.NEW, 160,
                Instant.ofEpochSecond(1687078800));
        Epic epic1 = new Epic("Эпик 1", "Выполнить эпик 1");
        final Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 3,
                25, Instant.ofEpochSecond(1687266000));
        final Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 3,
                15, Instant.ofEpochSecond(1687273200));
        final Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 3,
                60, Instant.ofEpochSecond(1687285800));
        Epic epic2 = new Epic("Эпик 2", "Выполнить эпик 2");

        taskManager.addTask(singleTask1);
        taskManager.addTask(singleTask2);
        taskManager.addEpic(epic1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        taskManager.addEpic(epic2);

        taskManager.getEpic(7);
        taskManager.getEpic(3);
        taskManager.getTask(1);
        taskManager.getSubtask(6);
        taskManager.getTask(2);
        taskManager.getEpic(3);
        taskManager.getSubtask(4);
        taskManager.getSubtask(6);
        taskManager.getSubtask(5);

        final List<Task> historyInManager = taskManager.getHistory();
        final List<Task> history = List.of(epic2, singleTask1, singleTask2, epic1, subtask1, subtask3, subtask2);

        assertEquals(history, historyInManager, "История заполнена неверно");

        taskManager.removeTask(2);
        taskManager.removeEpic(3);
        final List<Task> historyInManager2 = taskManager.getHistory();
        final List<Task> history2 = List.of(epic2, singleTask1);
        assertEquals(history2, historyInManager2, "История заполнена неверно после удаления задач");

        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        final List<Task> historyInManager3 = taskManager.getHistory();
        assertEquals(true, historyInManager3.isEmpty(), "История заполнена неверно после удаления всех задач");
    }

    @Test
    void getPrioritizedTasks() {
        SingleTask task1 = new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400));
        SingleTask task2 = new SingleTask("Задача 2", "Выполнить задачу 2",  TaskStatus.NEW, 160,
                Instant.ofEpochSecond(1687078800));
        Epic epic1 = new Epic("Эпик 1", "Выполнить эпик 1");
        Epic epic2 = new Epic("Эпик 2", "Выполнить эпик 2");
        final Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 3,
                25, Instant.ofEpochSecond(1687266000));
        final Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 3,
                15, Instant.ofEpochSecond(1687273200));
        final Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 3,
                60, Instant.ofEpochSecond(1687285800));

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        List<Task> tasksWithPriority = List.of(task2, task1, subtask1, subtask2, subtask3);

        assertNotNull(taskManager.getPrioritizedTasks(), "Список задач по приеоритету не заполнен");
        assertEquals(tasksWithPriority, taskManager.getPrioritizedTasks(), "Списки задач с приоритетом не одинаковы");

        final Subtask subtask4 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 3,
                60, Instant.ofEpochSecond(1687244400)); // в подзадаче время совпадает с временем задачи 1

        final SameTimeException exception = assertThrows( // проверка добавления задачи с пересечением во времени
                SameTimeException.class,
                () -> taskManager.addSubtask(subtask4)
        );
        assertEquals("Задачи пересекаются 8 и 1", exception.getMessage());

        List<Task> tasksWithPriorityWithSameStartTimeSubtask = List.of(task2, task1, subtask1, subtask2, subtask3);

        assertEquals(tasksWithPriorityWithSameStartTimeSubtask, taskManager.getPrioritizedTasks(), "Списки задач с приоритетом не одинаковы");
    }

}