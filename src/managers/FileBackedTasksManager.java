package managers;

import exceptions.ManagerSaveException;
import tasks.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final Path path;

    public FileBackedTasksManager (Path path) {
        this.path = path;
    }

    public static void main(String[] args) throws IOException {
        Path historyFile = Paths.get("C:\\Users\\alesh\\dev\\java-kanban\\src\\historyFile.csv");
        InMemoryTaskManager manager = new FileBackedTasksManager(historyFile);
        manager.addTask(new SingleTask("Задача 1", "Выполнить задачу 1", TaskStatus.NEW, 30,
                Instant.ofEpochSecond(1687244400))); // 20/06/ 07:00
        manager.addTask(new SingleTask("Задача 2", "Выполнить задачу 2",  TaskStatus.NEW, 160,
                Instant.ofEpochSecond(1687078800))); // 18/06/ 09:00
        manager.addEpic(new Epic("Эпик 1", "Выполнить эпик 1"));
        manager.addEpic(new Epic("Эпик 2", "Выполнить эпик 2"));
        manager.addSubtask(new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 3,
                25, Instant.ofEpochSecond(1687266000))); //20/06 13:00
        manager.addSubtask(new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 3,
                15, Instant.ofEpochSecond(1687273200))); //20/06 15:00
        manager.addSubtask(new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 3,
                60, Instant.ofEpochSecond(1687285800))); //20/06 18:30

        manager.getEpic(4);
        manager.getEpic(3);
        manager.getTask(1);
        manager.getSubtask(6);
        manager.getTask(2);
        manager.getEpic(3);
        manager.getSubtask(7);
        manager.getSubtask(6);
        manager.getSubtask(5);

        System.out.println(manager.getHistory());
        System.out.println(manager.getPrioritizedTasks());

        FileBackedTasksManager newManager = FileBackedTasksManager.loadFromFile(new File(
                "C:\\Users\\alesh\\dev\\java-kanban\\src\\historyFile.csv"));
        newManager.addSubtask(new Subtask("Подзадача 4", "Выполнить подзадачу 4", TaskStatus.IN_PROGRESS,
                4, 60, Instant.ofEpochSecond(1687271400))); // 20/06 14:30
        newManager.getSubtask(8);
        System.out.println(newManager.getHistory());
        System.out.println(manager.getPrioritizedTasks());
    }

    private void save() {
        try { // проверка наличия файла и его перезапись
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.createFile(path);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось найти файл для записи данных");
        }
        // запись текущего состояния менеджера
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path.toFile(), StandardCharsets.UTF_8))) {
            bufferedWriter.write("id,type,name,status,description,epic,duration,startTime" + '\n');

            String data = "";
            for (SingleTask singleTask : getListOfTasks()) {
                data += singleTask.toString() + '\n';
            }

            for (Epic epic : getListOfEpics()) {
                data += epic.toString() + '\n';
            }

            for (Subtask subtask : getListOfSubtasks()) {
                data += subtask.toString() + '\n';
            }

            data += "\n" + historyToString(getHistoryManager());
            bufferedWriter.write(data);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи в файл");
        }
    }

    private static String historyToString(HistoryManager manager) {
        List<Task> historyList = manager.getHistory();
        String idsHistory = "";
        for (Task task : historyList) {
            idsHistory += task.getId() + ",";
        }
        return idsHistory;
    }

    public static FileBackedTasksManager loadFromFile(File historyFile) throws IOException {
        List<String> tasksArray = new ArrayList<>();
        String history = "";
        FileBackedTasksManager manager = new FileBackedTasksManager(historyFile.toPath());
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(historyFile, StandardCharsets.UTF_8))) {
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (!line.equals("")) {
                    tasksArray.add(line);
                }
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка чтения файла");
        }

        if (!tasksArray.isEmpty()) { // удаляем строку заголовка и получаем строку с историей
            tasksArray.remove(0);
            if (!tasksArray.isEmpty()) { // проверка если в истории была только строка заголовка
                history = tasksArray.get(tasksArray.size() - 1);
                tasksArray.remove(tasksArray.size() - 1);
            }
        }
        int curId = 1;
        for (String string : tasksArray) {
            Task task = fromString(string);
            if (curId < task.getId()) {
                curId = task.getId();
            }
            addTaskWithoutSaving(manager, task);
            // добавляем id сабтасков в эпики, когда все задачи размещены в мапы
            for (Subtask subtask : manager.getSubtasks().values()) {
                manager.getEpics().get(subtask.getEpicId()).getSubtasksIds().add(subtask.getId());
            }

            manager.setNextId(curId + 1);
        }

        if (!tasksArray.isEmpty()) {
            List<Integer> listOfIds = historyFromString(history);
            for (Integer id : listOfIds) { // проверка наличия задачи в одной из трех мап и добавление в историю
                if (manager.getTasks().containsKey(id)) {
                    manager.getHistoryManager().add(manager.getTasks().get(id));
                } else if (manager.getEpics().containsKey(id)) {
                    manager.getHistoryManager().add(manager.getEpics().get(id));
                } else {
                    manager.getHistoryManager().add(manager.getSubtasks().get(id));
                }
            }
        }
        return manager;
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> listOfIds = new ArrayList<>();
        String[] historyIds = value.split(",");
        for (String id : historyIds) {
            listOfIds.add(Integer.parseInt(id));
        }
        return listOfIds;
    }

    private static void addTaskWithoutSaving(FileBackedTasksManager manager, Task task) {
        if (task instanceof Epic) {
            manager.getEpics().put(task.getId(), ((Epic) task));
        } else if (task instanceof Subtask) {
            manager.getSubtasks().put(task.getId(), (Subtask) task);
        } else if (task instanceof SingleTask) {
            manager.getTasks().put(task.getId(), (SingleTask) task);
        }
    }

    private static Task fromString(String value) { // конвертер строк из файла в задачи
        String[] taskElements = value.split(",");
        Task task = null;
        if (!taskElements[1].equals("")) {
            switch (taskElements[1]) {
                case "TASK":
                    task = new SingleTask(
                            taskElements[2],
                            taskElements[4],
                            taskStatusConverter(taskElements[3]),
                            Long.parseLong(taskElements[5]),
                            Instant.parse(taskElements[6])
                    );
                    task.setId(Integer.parseInt(taskElements[0]));
                    break;
                case "EPIC":
                    task = new Epic(taskElements[2], taskElements[4]);
                    task.setId(Integer.parseInt(taskElements[0]));
                    if (taskElements[6] != null) {
                        task.setStartTime(Instant.parse(taskElements[6]));
                    } else {
                        task.setStartTime(null);
                    }
                    break;
                case "SUBTASK":
                    task = new Subtask(
                            taskElements[2],
                            taskElements[4],
                            taskStatusConverter(taskElements[3]),
                            Integer.parseInt(taskElements[5]),
                            Long.parseLong(taskElements[6]),
                            Instant.parse(taskElements[7])
                    );
                    task.setId(Integer.parseInt(taskElements[0]));
                    break;
            }
        }
        return task;
    }

    private static TaskStatus taskStatusConverter(String value) { // метод для конвертации String в TaskStatus
        TaskStatus taskStatus = null;
        if (value.equals("NEW")) {
            taskStatus = TaskStatus.NEW;
        } else if (value.equals("IN_PROGRESS")) {
            taskStatus = TaskStatus.IN_PROGRESS;
        } else if (value.equals("DONE")) {
            taskStatus = TaskStatus.DONE;
        }
        return taskStatus;
    }

    // переопределение методов класса InMemoryTaskManager
    @Override
    public void addTask(SingleTask singleTask) {
        super.addTask(singleTask);
        save();
    }
    @Override
    public void updateTask(SingleTask newTask) {
        super.updateTask(newTask);
        save();
    }
    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }
    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }
    @Override
    public SingleTask getTask(int id) {
        SingleTask task = super.getTask(id);
        save();
        return task;
    }
    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }
    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }
    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }
    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }
    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }
    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }
    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }
    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }
    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }
    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }
}
