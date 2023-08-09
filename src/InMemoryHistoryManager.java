import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    static int MAX_SIZE = 10;

    private final List<Task> taskHistory = new ArrayList<>();

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(this.taskHistory);
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            if (this.taskHistory.size() == MAX_SIZE) {
                this.taskHistory.remove(0);
            }
            taskHistory.add(task);
        }
    }


}
