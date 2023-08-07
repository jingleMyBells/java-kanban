import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    static int MAX_SIZE = 10;

    private final ArrayList<Task> taskHistory = new ArrayList<>();

    @Override
    public ArrayList<Task> getHistory() {
        return this.taskHistory;
    }

    @Override
    public void add(Task task) {
        if (this.taskHistory.size() == MAX_SIZE) {
            this.taskHistory.remove(0);
        }
        taskHistory.add(task);
    }


}
