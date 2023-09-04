package ru.atlassian.jira.model;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description, Status.NEW);
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
        return this.getClass() + "{" +
                "id=" + this.id +
                ", title='" + this.title + '\'' +
                ", description='" + this.description + '\'' +
                ", epicId='" + this.epicId + '\'' +
                ", status=" + this.status +
                '}';
    }
}
