package ru.atlassian.jira.model;
import java.util.ArrayList;
import java.util.List;


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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return this.getId() == epic.getId();
    }

    @Override
    public String toString() {
//        return this.getClass() + "{" +
//                "id=" + this.id +
//                ", title='" + this.title + '\'' +
//                ", description='" + this.description + '\'' +
//                ", status=" + this.status +
//                ", subtasks" + this.tasks + '\'' +
//                '}';
        return String.join(
                ",", String.valueOf(id), "Epic",
                title, status.toString(), description
        );
    }

}
