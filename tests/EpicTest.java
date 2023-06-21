package tests;

import managers.InMemoryTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.TaskStatus;

import java.time.Instant;

class EpicTest {
    private static TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
        taskManager.addEpic(new Epic("Эпик 1", "Выполнить эпик 1"));
    }

    // методы проверки корректности присвоения статуса эпику
    @Test
    public void checkIfEpicStatusIsNewWhenNoSubtasks() {
        final TaskStatus taskStatus = taskManager.getEpics().get(1).getStatus();
        Assertions.assertEquals("NEW", taskStatus.toString());
    }

    @Test
    public void checkIfEpicStatusIsNewWhenAllSubtasksStatusesAreNew() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                25, Instant.ofEpochSecond(1687266000)); //20/06 13:00
        Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.NEW, 1,
                15, Instant.ofEpochSecond(1687273200)); //20/06 15:00
        Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.NEW, 1,
                60, Instant.ofEpochSecond(1687285800)); //20/06 18:30
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        final TaskStatus taskStatus = taskManager.getEpics().get(1).getStatus();
        Assertions.assertEquals("NEW", taskStatus.toString());
    }
    @Test
    public void checkIfEpicStatusIsDoneWhenAllSubtasksStatusesAreDone() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.DONE, 1,
                25, Instant.ofEpochSecond(1687266000)); //20/06 13:00
        Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.DONE, 1,
                15, Instant.ofEpochSecond(1687273200)); //20/06 15:00
        Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.DONE, 1,
                60, Instant.ofEpochSecond(1687285800)); //20/06 18:30
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        final TaskStatus taskStatus = taskManager.getEpics().get(1).getStatus();
        Assertions.assertEquals("DONE", taskStatus.toString());
    }

    @Test
    public void checkIfEpicStatusIsInProgressWhenAllSubtasksStatusesAreInProgress() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.IN_PROGRESS, 1,
                25, Instant.ofEpochSecond(1687266000)); //20/06 13:00
        Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.IN_PROGRESS, 1,
                15, Instant.ofEpochSecond(1687273200)); //20/06 15:00
        Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.IN_PROGRESS, 1,
                60, Instant.ofEpochSecond(1687285800)); //20/06 18:30
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        final TaskStatus taskStatus = taskManager.getEpics().get(1).getStatus();
        Assertions.assertEquals("IN_PROGRESS", taskStatus.toString());
    }
    @Test
    public void checkIfEpicStatusIsInProgressWhenSubtasksHaveDifferentStatuses() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Выполнить подзадачу 1", TaskStatus.NEW, 1,
                25, Instant.ofEpochSecond(1687266000)); //20/06 13:00
        Subtask subtask2 = new Subtask("Подзадача 2", "Выполнить подзадачу 2",  TaskStatus.DONE, 1,
                15, Instant.ofEpochSecond(1687273200)); //20/06 15:00
        Subtask subtask3 = new Subtask("Подзадача 3", "Выполнить подзадачу 3", TaskStatus.DONE, 1,
                60, Instant.ofEpochSecond(1687285800)); //20/06 18:30
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        final TaskStatus taskStatus = taskManager.getEpics().get(1).getStatus();
        Assertions.assertEquals("IN_PROGRESS", taskStatus.toString());
    }
}