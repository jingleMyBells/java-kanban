package ru.atlassian.jira.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class Task implements Comparable<Task>{
    protected int id;
    protected String title;
    protected String description;
    protected Status status;
//    protected Duration duration;
    protected int duration;
    protected LocalDateTime startTime;

    public static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy:HH.mm");

    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(
            String title,
            String description,
            Status status,
            int duration,
            LocalDateTime startTime
    ) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Optional<Integer> getDuration() {
        return Optional.of(this.duration);
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(this.startTime);
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Optional<LocalDateTime> getEndTime() {
        if (getStartTime().isPresent()) {
            return Optional.of(getStartTime().get().plus(Duration.ofMinutes(duration)));
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status);
    }

    @Override
    public String toString() {
        String taskDuration = "0";
        if (getDuration().isPresent()) {
            taskDuration = String.valueOf(duration);
        }
        String dateTime = "0";
        if (getStartTime().isPresent()) {
            dateTime = getStartTime().get().format(FORMATTER);
        }
        return String.join(
                ",", String.valueOf(id), "Task",
                title, status.toString(), description, taskDuration, dateTime
        );
    }

    @Override
    public int compareTo(Task task) {
        if (this.getStartTime().isPresent() && task.getStartTime().isPresent()) {
            LocalDateTime currentTaskStart = this.getStartTime().get();
            LocalDateTime anotherTaskStart = task.getStartTime().get();
            if (anotherTaskStart.isBefore(currentTaskStart)) {
                return 1;
            } else if (anotherTaskStart.isAfter(currentTaskStart)) {
                return -1;
            }
            return 0;
        } else if (this.getStartTime().isPresent() && task.getStartTime().isEmpty()) {
            return -1;
        } else if (this.getStartTime().isEmpty() && task.getStartTime().isPresent()) {
            return 1;
        } else {
            return 0;
        }
    }

}
