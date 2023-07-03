package managers;

import api.KVServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

public class Managers {
    public static InMemoryTaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }
    public static FileBackedTasksManager getDefaultFileBackedTasksManager() {
        return new FileBackedTasksManager("C:\\Users\\alesh\\dev\\java-kanban\\src\\historyFile.csv");
    }
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
    public static HttpTaskManager getDefault() throws IOException, InterruptedException {
        return new HttpTaskManager("http://localhost:" + KVServer.PORT);
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        Gson gson = gsonBuilder.create();
        return gson;
    }
}