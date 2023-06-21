package managers;

import tasks.Epic;
import tasks.SingleTask;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface TaskManager {
    HashMap<Integer, SingleTask> getTasks();
    HashMap<Integer, Subtask> getSubtasks();
    HashMap<Integer, Epic> getEpics();

    // блок методов для работы с простыми задачами
    void addTask(SingleTask singleTask); // добавить задачу
    void updateTask(SingleTask newTask); // обновить задачу
    ArrayList<SingleTask> getListOfTasks(); // получить список задач
    void removeTask(int id); // удалить задачу
    void removeAllTasks(); // удалить все задачи
    SingleTask getTask(int id); // получить задачу

    // блок методов для работы с эпиками
    void addEpic(Epic epic); // добавить эпик
    void updateEpic(Epic epic); // обновить эпик
    ArrayList<Epic> getListOfEpics(); // получить список эпиков
    ArrayList<Subtask> getListOfEpicSubtasks(int epicId); // получить список подзадач эпика
    void removeEpic(int id); // удалить эпик (и его подзадачи)
    void removeAllEpics(); // удалить все эпики (и их подзадачи)
    Epic getEpic(int id); // получить эпик

    // блок методов для работы с подзадачами
    void addSubtask(Subtask subtask); // добавить подзадачу
    void updateSubtask(Subtask subtask); // обновить подзадачу
    ArrayList<Subtask> getListOfSubtasks(); // получить список подзадач
    void removeSubtask(int id); // удалить подзадачу
    void removeAllSubtasks(); // удалить все подзадачи
    Subtask getSubtask(int id); // получить подзадачу

    List<Task> getHistory();

    // методы работы с продолжительность задач
    void setStartAndEndTimeToEpic(int id);
    ArrayList<Task> getPrioritizedTasks();
}
