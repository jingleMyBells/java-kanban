import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList taskHistory = new CustomLinkedList();
    private Map<Integer, Node> historyMap = new HashMap<>();


    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(this.taskHistory.getTasks());
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            Node newNode = taskHistory.linkLast(task);
            this.remove(task.getId());
            historyMap.put(task.getId(), newNode);
        }
    }

    @Override
    public void remove(int id) {
        if (historyMap.containsKey(id)) {
            taskHistory.removeNode(historyMap.get(id));
        }
    }


    public class CustomLinkedList{
        public Node head;
        public Node tail;

        public Node linkLast(Task task) {
            Node newNode = new Node(task);
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
                Node currentNode = head;
                tasks.add(head.task);
                while (currentNode.next != null) {
                    tasks.add(currentNode.task);
                    currentNode = currentNode.next;
                }
            }
            return new ArrayList<>(tasks);
        }

        public void removeNode(Node node) {
            node.next.prev = node.prev.next;
            node.prev = null;
            node.next = null;
        }




    }
}
