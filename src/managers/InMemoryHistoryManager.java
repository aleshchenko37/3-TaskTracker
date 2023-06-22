package managers;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Node<Task> head;
    private Node<Task> tail;

    private final Map<Integer, Node<Task>> taskHistory = new HashMap<>();

    private boolean isEmpty() { // метод проверки истории на наличие задач
        return head == null;
    }

    private void removeNode(Node<Task> node) { // метод удаляет узел, переопределяя ссылки в других узлах
        if (head == null || node == null) {
            return;
        }

        if (head == node) {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        }
    }

    private void linkLast(Node<Task> node) { // метод добавляет узел в конец списка, переопределяя ссылки на другие узлы
        if(isEmpty()) {
            head = node;
        } else {
            tail.next = node;
        }
        node.prev = tail;
        tail = node;
    }

    public List<Task> getTasks() { // все задачи в списке без повторов в последовательности вызова
        List<Task> historyInList = new ArrayList<>();
        Node<Task> current = head;
        while (current != null) {
            historyInList.add(current.getTask());
            current = current.next;
        }
        return historyInList;
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (taskHistory.containsKey(task.getId())) {
            removeNode(taskHistory.get(task.getId()));
            taskHistory.remove(task.getId());
        }
        Node<Task> node = new Node<>(task);
        taskHistory.put(task.getId(), node);
        linkLast(node);
    }

    @Override
    public void remove(int id) {
        removeNode(taskHistory.get(id));
        taskHistory.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
