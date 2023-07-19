import java.util.ArrayList;
public class Epic extends Task{
    private ArrayList<Subtask> tasks;
    public Epic(int id, String title, String description) {
        super(id, title, description);
        tasks = new ArrayList<Subtask>();
    }

    public ArrayList<Subtask> getTasks() {
        return tasks;
    }

    public void addSubtask(Subtask subtask) {
        this.tasks.add(subtask);
    }
}
