import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList<Task> taskHistory = new CustomLinkedList<>();
    private Map<Integer, Node<Task>> historyMap = new HashMap<>();


    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(this.taskHistory.getTasks());
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            Node<Task> newNode = taskHistory.linkLast(task);
            if (historyMap.containsKey(task.getId())) {
                taskHistory.removeNode(historyMap.get(task.getId()));
            }
            historyMap.put(task.getId(), newNode);
        }
    }

    @Override
    public void remove(int id) {
        taskHistory.removeNode(historyMap.get(id));
    }

    public class CustomLinkedList<Task>{
        public Node<Task> head;
        public Node<Task> tail;

        public Node<Task> linkLast(Task task) {
            Node<Task> newNode = new Node<>(task);
            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                tail.next = newNode;
            }
            return tail;
        }

        public ArrayList<Task> getTasks() {
            List<Task> tasks = new ArrayList<>();
            if (head != null) {
                Node<Task> currentNode = head;
                tasks.add(head.task);
                while (currentNode.next != null) {
                    tasks.add(currentNode.task);
                    currentNode = currentNode.next;
                }
            }
            return new ArrayList<>(tasks);
        }

        public void removeNode(Node<Task> node) {
            node.next.prev = node.prev.next;
            node.prev = null;
            node.next = null;
        }




    }
}
