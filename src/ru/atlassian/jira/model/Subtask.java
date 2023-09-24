package ru.atlassian.jira.model;

import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description, Status.NEW);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, int epicId, int duration, LocalDateTime startTime) {
        super(title, description, Status.NEW, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subtask subtask = (Subtask) o;
        return this.getId() == subtask.getId();
    }

    @Override
    public String toString() {
        String taskDuration = "0";
        if (getDuration().isPresent()) {
            taskDuration = String.valueOf(getDuration().get().toMinutes());
        }
        String dateTime = "0";
        if (getStartTime().isPresent()) {
            dateTime = getStartTime().get().format(FORMATTER);
        }
        return String.join(
                ",", String.valueOf(id), "Subtask",
                title, status.toString(), description, String.valueOf(epicId), taskDuration, dateTime
        );
    }
}
