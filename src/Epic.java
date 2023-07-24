import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Epic extends Task{
    private ArrayList<Subtask> tasks;
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

    public ArrayList<Subtask> getAllEpicSubtasks() {
        return tasks;
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
    public int hashCode() {
        return Objects.hash(this.getId(), this.getTitle(), this.getDescription(), this.getStatus());
    }

    @Override
    public String toString() {
        return this.getClass() + "{" +
                "id=" + this.getId() +
                ", title='" + this.getTitle() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", status=" + this.getStatus() +
                ", subtasks" + this.getAllEpicSubtasks() + '\'' +
                '}';
    }

}
