import java.util.Collection;
import java.util.HashMap;

public class TaskManager {

    private int autoIncrement;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;

    TaskManager() {
        this.autoIncrement = 0;
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
    }

    private int getAutoIncrement() {
        return this.autoIncrement;
    }

    private int createNewTaskId() {
        return ++this.autoIncrement;
    }

    public void saveTask(Task task) {
        if (task.getId() == 0) {
            task.setId(createNewTaskId());
        }
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

    public void saveEpic(Epic epic) {
        if (epic.getId() == 0) {
            epic.setId(createNewTaskId());
        }
        this.epics.put(epic.getId(), epic);
    }

    public Collection<Epic> getAllEpics() {
        return this.epics.values();
    }

    public void deleteAllEpics() {
        /*
        * По ТЗ непонятно, надо ли делать что-либо
        * с подазадчами при удалении эпиков
        */
        this.epics.clear();
    }

    public Epic getEpicById(int id) {
        return this.epics.get(id);
    }

    public void deleteEpicById(int id) {
        /*
         * По ТЗ непонятно, надо ли делать что-либо
         * с подазадчами при удалении эпиков
         */
        this.epics.remove(id);
    }

    public void saveSubtask(Subtask subtask) {
        if (subtask.getId() == 0) {
            subtask.setId(createNewTaskId());
        }
        this.subtasks.put(subtask.getId(), subtask);
        Epic epic = this.epics.get(subtask.getEpicId());
        epic.addSubtask(subtask);
        epic.checkAndModifyStatus();
    }

    public Collection<Subtask> getAllSubtasks() {
        return this.subtasks.values();
    }

    public void deleteAllSubtasks() {
        this.subtasks.clear();
        for (Epic epic : this.getAllEpics()) {
            epic.checkAndModifyStatus();
        }
    }

    public Subtask getSubtaskById(int id) {
        return this.subtasks.get(id);
    }

    public void deleteSubtaskById(int id) {
        Epic epic = this.epics.get(this.subtasks.get(id).getEpicId());
        epic.removeSubtask(this.getSubtaskById(id));
        this.subtasks.remove(id);
        epic.checkAndModifyStatus();
    }
}
