package ru.atlassian.jira.model;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class Epic extends Task {
    private final List<Subtask> tasks;
    public Epic(String title, String description) {
        super(title, description, Status.NEW);
        tasks = new ArrayList<>();
    }

    public List<Subtask> getTasks() {
        return tasks;
    }

    public void addSubtask(Subtask subtask) {
        if (!this.tasks.contains(subtask)) {
            this.tasks.add(subtask);
        }
    }

    public void removeSubtask(Subtask subtask) {
        this.tasks.remove(subtask);
    }

    @Override
    public Optional<LocalDateTime> getStartTime() {
        if (tasks.isEmpty()) {
            return Optional.empty();
        }
        List<Subtask> subtaskToSort = new ArrayList<>(this.tasks);
        Collections.sort(subtaskToSort);
        Subtask firstSubTask = subtaskToSort.get(0);
        if (firstSubTask.getStartTime().isPresent()) {
            return firstSubTask.getStartTime();
        }
        return Optional.empty();
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        if (tasks.isEmpty()) {
            return Optional.empty();
        }
        List<Subtask> subtaskToSort = new ArrayList<>(this.tasks);
        Collections.sort(subtaskToSort);
        Subtask firstSubTask = subtaskToSort.get(0);
        LocalDateTime lastEndTime = LocalDateTime.MIN;
        if (firstSubTask.getEndTime().isEmpty()) {
            return Optional.empty();
        }
        for (Subtask subtask : subtaskToSort) {
            if (subtask.getEndTime().isPresent()) {
                LocalDateTime currentSubTaskEnd = subtask.getEndTime().get();
                if (currentSubTaskEnd.isAfter(lastEndTime)) {
                    lastEndTime = currentSubTaskEnd;
                }
            } else {
                break;
            }
        }
        if (lastEndTime.isAfter(LocalDateTime.MIN)) {
            return Optional.of(lastEndTime);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Duration> getDuration() {
        Duration cumulativeDuration = Duration.ofMinutes(0);
        for (Subtask subtask : tasks) {
            if (subtask.getDuration().isPresent()) {
                cumulativeDuration = cumulativeDuration.plus(subtask.getDuration().get());
            }
        }
        if (cumulativeDuration.compareTo(Duration.ofMinutes(0)) == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(cumulativeDuration);
    }

    //TODO: ВОЗМОЖНО НАДО ЗАПРЕТИТЬ СЕТТЕРЫ ДЛЯ РАССЧЕТНЫХ ПОЛЕЙ ЭПИКА

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return this.getId() == epic.getId();
    }

    @Override
    public String toString() {
        return String.join(
                ",", String.valueOf(id), "Epic",
                title, status.toString(), description
        );
    }

}
