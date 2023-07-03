package managers;

import api.KVTaskClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tasks.*;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.List;

public class HttpTaskManager extends FileBackedTasksManager {
    private static Gson gson;
    private KVTaskClient kvTaskClient;
    private static String key;
    public HttpTaskManager(String uri) throws IOException, InterruptedException {
        super(uri);
        kvTaskClient = new KVTaskClient(URI.create(uri));
        this.gson = Managers.getGson();
      }

    public KVTaskClient getKvTaskClient() {
        return kvTaskClient;
    }

    @Override
    protected void save() { // метод заменяет сохранение данных в файл на сохранение в KVServer
        String taskManagerInString = "";
        for (SingleTask task : getTasks().values()) { // все значения из мапы задач добавляем в строку с состоянием менеджера
            taskManagerInString += gson.toJson(task) + "\n";
        }
        for (Epic epic : getEpics().values()) { // все значения из мапы эпиков добавляем в строку с состоянием менеджера
            taskManagerInString += gson.toJson(epic) + "\n";
        }
        for (Subtask subtask : getSubtasks().values()) { // все значения из мапы подзадач добавляем в строку с состоянием менеджера
            taskManagerInString += gson.toJson(subtask) + "\n";
        }

        taskManagerInString += "\n" + historyToString(getHistoryManager());

        try {
            kvTaskClient.put(kvTaskClient.getAPI_TOKEN(), taskManagerInString);
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public static HttpTaskManager load(String key) {
        HttpTaskManager.key = key; // метод заменяет загрузку данных из файла на загрузку из KVServer
        try {
            HttpTaskManager httpTaskManager = new HttpTaskManager("http://localhost:8077");
            String taskManagerInString = httpTaskManager.getKvTaskClient().load(key); // получаем состояние менеджера с сервера
            if (!taskManagerInString.equals("")) {
                String[] tasks = taskManagerInString.split("\n");
                int curId = 1;
                for (int i = 0; i < tasks.length - 1; i++) { // последняя строка - история
                    if (!tasks[i].equals("")) {
                        Task task = fromString(tasks[i]);
                        if (task != null && curId < task.getId()) {
                            curId = task.getId();
                        }
                        HttpTaskManager.addTaskWithoutSaving(httpTaskManager, task);
                        httpTaskManager.setNextId(curId + 1);
                    }
                    for (Subtask subtask : httpTaskManager.getSubtasks().values()) {
                        httpTaskManager.getEpics().get(subtask.getEpicId()).getSubtasksIds().add(subtask.getId());
                    }
                }

                String historyInString = tasks[tasks.length - 1];
                List<Integer> listOfIds = historyFromString(historyInString);
                for (Integer id : listOfIds) { // проверка наличия задачи в одной из трех мап и добавление в историю
                    if (httpTaskManager.getTasks().containsKey(id)) {
                        httpTaskManager.getHistoryManager().add(httpTaskManager.getTasks().get(id));
                    } else if (httpTaskManager.getEpics().containsKey(id)) {
                        httpTaskManager.getHistoryManager().add(httpTaskManager.getEpics().get(id));
                    } else {
                        httpTaskManager.getHistoryManager().add(httpTaskManager.getSubtasks().get(id));
                    }
                }
                return httpTaskManager;
            } else {
                System.out.println("Менеджер на сервере пуст");
                return null;
            }
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }


    private static Task fromString(String value) throws IOException {
        JsonElement jsonElement = JsonParser.parseString(value);
        if (!jsonElement.isJsonObject()) {
            throw new IOException("Неверный формат Json");
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.toString().contains("epicId")) {
            Subtask subtask = gson.fromJson(jsonObject, Subtask.class);
/*            Subtask subtask = new Subtask(jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString(), jsonObject.get("id").getAsInt(),
                    TaskStatus.valueOf(jsonObject.get("taskStatus").getAsString()),
                    jsonObject.get("epicId").getAsInt(),
                    Long.parseLong(jsonObject.get("duration").getAsString()),
                    Instant.parse(jsonObject.get("startTime").getAsString()));*/
            return subtask;
        } else if (jsonObject.toString().contains("subtasksIds")) {
            Epic epic = gson.fromJson(jsonObject, Epic.class);
            return epic;
        } else {
            SingleTask task = gson.fromJson(jsonObject, SingleTask.class);
            return task;
        }
    }
}
