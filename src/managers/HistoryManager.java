package managers;

import tasks.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task); // feat: метод добавления задачи в историю

    void remove(int id);

    List<Task> getHistory(); // feat: метод получения истории задач
}
