import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface TaskManager {

    void createTask(Task task);

    void updateTask(Task task);

    Collection<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    void deleteTaskById(int id);

    void createEpic(Epic epic);

    void updateEpic(Epic epic);

    Collection<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpicById(int id);

    void deleteEpicById(int id);

    List<Subtask> getAllEpicSubtasks(Epic epic);

    void checkAndModifyEpicStatus(Epic epic);

    void createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    Collection<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(int id);

    void deleteSubtaskById(int id);

    HistoryManager getHistoryManager();
}
