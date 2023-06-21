package tasks;

import java.time.Instant;
import java.util.ArrayList;

public class Epic extends Task {
    protected Instant endTime;
    private ArrayList<Integer> subtasksIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW, 0, Instant.ofEpochSecond(0));
    }

    @Override
    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public ArrayList<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    @Override
    public String toString() {
        return getId() + "," + "EPIC" + "," + getName() + "," + getStatus() + "," + getDescription() + "," + getDuration() + "," +
                getStartTime();
    }
}