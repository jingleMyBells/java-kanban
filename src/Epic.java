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

    public void checkAndModifyStatus() {
        HashMap<Status, Integer> tasksStatuses = new HashMap<>();
        tasksStatuses.put(Status.NEW, 0);
        tasksStatuses.put(Status.DONE, 0);
        tasksStatuses.put(Status.IN_PROGRESS, 0);
        for (Subtask task : tasks) {
            int statusCounter = tasksStatuses.get(task.getStatus());
            tasksStatuses.put(task.getStatus(), ++statusCounter);
        }
        int tasksCount = this.tasks.size();

        if (tasksCount == 0) {
            this.setStatus(Status.NEW);
        } else {
            if (tasksStatuses.get(Status.NEW) == tasksCount) {
                this.setStatus(Status.NEW);
            } else if (tasksStatuses.get(Status.DONE) == tasksCount) {
                this.setStatus(Status.DONE);
            } else {
                this.setStatus(Status.IN_PROGRESS);
            }
        }
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
