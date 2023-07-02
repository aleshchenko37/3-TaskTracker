package managers;

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
/*    public static HttpTaskManager getDefaultHttpTaskManager() throws IOException, InterruptedException {
        return new HttpTaskManager("http://localhost:8080/");
    }*/
}