package tasks;

import java.time.Instant;
import java.util.Objects;

public abstract class Task {

    private String name;
    private String description;
    private int id;
    private TaskStatus taskStatus;
    private long duration;
    protected Instant startTime;
    //protected Instant endTime;

    public Task(String name, String description, TaskStatus taskStatus, long duration, Instant startTime) {
        this.name = name;
        this.description = description;
        this.id = 0;
        this.taskStatus = taskStatus;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(String name, String description, int id, TaskStatus taskStatus, long duration, Instant startTime) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.taskStatus = taskStatus;
        this.duration = duration;
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int taskId) {
        this.id = taskId;
    }

    public TaskStatus getStatus() {
        return taskStatus;
    }

    public void setStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public long getDuration() { return duration;}

    public void setDuration(long duration) { this.duration = duration; }

    public Instant getStartTime() { return startTime; }

    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() {
        return startTime.plusSeconds(duration * 60);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name) && Objects.equals(description, task.description)
                && Objects.equals(taskStatus, task.taskStatus);
    }

    @Override
    public String toString() {
        return id + "," + name + "," + taskStatus + "," + description + "," + duration + "," + startTime.toString();
    }
}
