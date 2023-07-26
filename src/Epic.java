import java.util.ArrayList;

public class Epic extends Task{
    private final ArrayList<Subtask> tasks;
    public Epic(String title, String description) {
        super(title, description, Status.NEW);
        tasks = new ArrayList<>();
    }

    public ArrayList<Subtask> getTasks() {
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
        return this.getClass() + "{" +
                "id=" + this.id +
                ", title='" + this.title + '\'' +
                ", description='" + this.description + '\'' +
                ", status=" + this.status +
                ", subtasks" + this.tasks + '\'' +
                '}';
    }

}
