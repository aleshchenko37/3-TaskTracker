package managers;

import exceptions.SameTimeException;
import exceptions.TaskNotFoundException;
import tasks.*;

import java.time.Instant;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;

    private final HistoryManager historyManager = managers.Managers.getDefaultHistory();
    private final HashMap<Integer, SingleTask> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    Comparator<Task> comparator = new StartTimeComparator().thenComparing(new IdComparator());

    private final Set<Task> tasksWithPriority = new TreeSet<>(comparator);

    public int getNextId() {
        return nextId;
    }
    public void setNextId(int nextId) {
        this.nextId = nextId;
    }
    protected HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public HashMap<Integer, SingleTask> getTasks() {
        return tasks;
    }
    @Override
    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }
    @Override
    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
    public Set<Task> getTasksWithPriority() {
        return tasksWithPriority;
    }

    @Override
    public void addTask(SingleTask task) {
        if (task != null) {
            task.setId(nextId);
            nextId++;
            tasks.put(task.getId(), task);
            addToTasksWithPriority(task);
        }
    }

    @Override
    public void updateTask(SingleTask newTask) { // объект класса содержит id старой задачи
        if (newTask != null && getTasks().containsKey(newTask.getId())) {
            checkTimeIntersections(newTask);
            tasks.put(newTask.getId(), newTask); // задача перезаписана
        }
    }

    @Override
    public ArrayList<SingleTask> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeTask(int id) {
        if (tasks.containsKey(id)) {
            historyManager.remove(id);
            tasks.remove(id);
        }
    }

    @Override
    public void removeAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public SingleTask getTask(int id) throws TaskNotFoundException {
        if (!tasks.containsKey(id)) {
            throw new TaskNotFoundException("Задача не найдена");
        }
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic != null) {
            epic.setId(nextId);
            nextId++;
            setStartAndEndTimeToEpic(epic.getId());
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void updateEpic(Epic newEpic) {
        if (newEpic != null && getEpics().containsKey(newEpic.getId())) {
            setStartAndEndTimeToEpic(newEpic.getId());
            epics.put(newEpic.getId(), newEpic);
        }
    }

    @Override
    public ArrayList<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getListOfEpicSubtasks(int epicId) {
        if (epics.containsKey(epicId)) {
            ArrayList<Subtask> listOfEpicSubtasks = new ArrayList<>();
            Epic epic = epics.get(epicId);
            for (Integer subtaskId : epic.getSubtasksIds()) {
                listOfEpicSubtasks.add(subtasks.get(subtaskId));
            }
            return listOfEpicSubtasks;
        } else {
            throw new TaskNotFoundException("Эпик не найден");
        }
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
            epics.remove(id);
        }
    }

    @Override
    public void removeAllEpics() {
        for (Epic epic : epics.values()) {
            for (Integer subtaskId : epic.getSubtasksIds()) {
                historyManager.remove(subtaskId);
            }
            historyManager.remove(epic.getId());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpic(int id) throws TaskNotFoundException {
        if (!epics.containsKey(id)) {
            throw new TaskNotFoundException("Задача не найдена");
        }
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (subtask != null && epics.containsKey(epicId)) { // проверка, есть ли эпик, к которому относится подзадача
            subtask.setId(nextId);
            addToTasksWithPriority(subtask); // если есть пересечение во времени, подзадача не добавится
            nextId++;
            subtasks.put(subtask.getId(), subtask);
            epics.get(epicId).getSubtasksIds().add(subtask.getId());
            updateEpicStatus(epicId);
            setStartAndEndTimeToEpic(epicId);
        } else {
            throw new TaskNotFoundException("Передан неверный id эпика");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (subtask != null && epics.containsKey(epicId)) {
            checkTimeIntersections(subtask);
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(epicId);
            setStartAndEndTimeToEpic(epicId);
        }
    }

    private void updateEpicStatus(int epicId) { // refactor: работа метода с учетом добавленного списка TaskStatus
        Epic epic = epics.get(epicId);
        if (checkIfEpicDone(epic)) {
            epic.setStatus(TaskStatus.DONE);
        } else if (checkIfNew(epic)) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private boolean checkIfEpicDone(Epic epic) { // refactor: работа метода с учетом добавленного списка TaskStatus
        boolean isDone = true;
        for (Integer subtasksId : epic.getSubtasksIds()) {
            Subtask curSubtask = subtasks.get(subtasksId);
            if (curSubtask.getStatus() != TaskStatus.DONE) {
                isDone = false;
            }
        }
        return isDone;
    }

    private boolean checkIfNew(Epic epic) { // refactor: работа метода с учетом добавленного списка TaskStatus
        boolean isNew = true;
        for (Integer subtasksId : epic.getSubtasksIds()) {
            Subtask curSubtask = subtasks.get(subtasksId);
            if (curSubtask.getStatus() != TaskStatus.NEW) {
                isNew = false;
            }
        }
        return isNew;
    }

    @Override
    public ArrayList<Subtask> getListOfSubtasks() {
        ArrayList<Subtask> listOfSubtasks = new ArrayList<>();
        listOfSubtasks.addAll(subtasks.values()); // refactor: цикл for заменен на addAll()
        return listOfSubtasks;
    }

    @Override
    public void removeSubtask(int id) { // пр. id = 123
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            epics.get(epicId).getSubtasksIds().remove(Integer.valueOf(id)); // чтобы удалился объект 123, а не объект
            // с индексом 123, id преобразовывается в Integer, а метод .remove(int id) - в метод .remove(Object o)
            updateEpicStatus(epicId);
            setStartAndEndTimeToEpic(epicId);
            historyManager.remove(id);
            subtasks.remove(id);
        }
    }

    @Override
    public void removeAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        for (Epic epic : epics.values()) {
            epic.getSubtasksIds().clear();
            epic.setStatus(TaskStatus.NEW);
            setStartAndEndTimeToEpic(epic.getId());
        }
        subtasks.clear();
    }
    @Override
    public Subtask getSubtask(int id) throws TaskNotFoundException {
        if (!subtasks.containsKey(id)) {
            throw new TaskNotFoundException("Задача не найдена");
        }
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public void setStartAndEndTimeToEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
            if (epic.getStartTime() == Instant.ofEpochSecond(0)) { // если время старта эпика задано по умолчанию точкой начала Unix Epoch
                epic.setStartTime(Instant.MAX); // ему присваивается максимальное время старта, чтобы время сабтасков всегда было раньше
            }
            Instant epicStartTime = epic.getStartTime();
            long totalTime = 0;

            if (!subtasksIds.isEmpty()) {
                for (Integer subtaskId : subtasksIds) {
                    Instant subtaskStartTime = subtasks.get(subtaskId).getStartTime();
                    long subtaskDuration = subtasks.get(subtaskId).getDuration();
                    if (subtaskStartTime.isBefore(epicStartTime)) { // если старт подзадачи раньше старта эпика, присваивается время старта подзадачи
                        epicStartTime = subtaskStartTime;
                    }
                    totalTime += subtaskDuration;
                }
                epic.setStartTime(epicStartTime);
                epic.setEndTime(epic.getStartTime().plusSeconds(totalTime));
                //addToTasksWithPriority(epic);
            } else {
                epic.setStartTime(null);
                epic.setEndTime(null);
                epic.setDuration(0);
            }
        }
    }

    @Override
    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(tasksWithPriority);
        // эпики не попадают в список, так как у них нет собственного времени начала и конца
    }

    private void addToTasksWithPriority(Task task) {
        checkTimeIntersections(task);
        tasksWithPriority.add(task);
    }

    private void checkTimeIntersections(Task task) {
        List<Task> tasksWithPriority = getPrioritizedTasks();
        for (int i = 1; i < tasksWithPriority.size(); i++) {
            if (task.getStartTime().isBefore(tasksWithPriority.get(i).getEndTime()) ||
                    tasksWithPriority.get(i).getStartTime().isAfter(task.getEndTime())) {
                throw new SameTimeException("Задачи пересекаются "
                        + task.getId()
                        + " и "
                        + tasksWithPriority.get(i).getId());
            }
        }
    }

    public class StartTimeComparator implements Comparator<Task> {
        // компаратор для сравнения времени начала задач
        @Override
        public int compare(Task task1, Task task2) {
            return task1.getStartTime().compareTo(task2.getStartTime());
        }
    }

    public class IdComparator implements Comparator<Task> {
        // компаратор для сравнения задач по id
        @Override
        public int compare(Task task1, Task task2) {
            return task1.getId() - task2.getId();
        }
    }
}
