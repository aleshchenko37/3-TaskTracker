package tasks;

import java.time.Instant;

public class SingleTask extends Task {

    public SingleTask(String name, String description, TaskStatus taskStatus, long duration, Instant startTime) {
        super(name, description, taskStatus, duration, startTime);
    }

    public SingleTask(String name, String description, int id, TaskStatus taskStatus, long duration, Instant startTime) {
        super(name, description, id, taskStatus, duration, startTime);
    }

    public String toString() {
        return getId() + "," + "TASK" + "," + getName() + "," + getStatus() + "," + getDescription() + "," + getDuration() + "," + startTime.toString();
    }
}
