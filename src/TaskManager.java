import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
public class TaskManager {

    private int autoIncrement;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;

    TaskManager() {
        this.autoIncrement = 0;
    }

    private int getAutoIncrement() {
        return this.autoIncrement;
    }

    private void increaseAutoIncrement() {
        this.autoIncrement++;
    }

    public void saveTask(Task task) {
        this.tasks.put(task.getId(), task);
    }

    public Collection<Task> getAllTasks() {
        return this.tasks.values();
    }

    public void deleteAllTasks() {
        this.tasks.clear();
    }

    public Task getTaskById(int id) {
        return this.tasks.get(id);
    }

    public void deleteTaskById(int id) {
        this.tasks.remove(id);
    }



}
