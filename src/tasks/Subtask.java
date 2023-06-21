package tasks;

import java.time.Instant;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, TaskStatus taskStatus, int epicId, long duration, Instant startTime) {
        super(name, description, taskStatus, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public String toString() {
        return getId() + "," + "SUBTASK" + "," + getName() + "," + getStatus() + "," + getDescription() + "," + getEpicId() + "," + getDuration() + "," + startTime.toString();
    }
}
