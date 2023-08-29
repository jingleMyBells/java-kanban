import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private Node head = null;
    private Node tail = null;
    private final Map<Integer, Node> historyMap = new HashMap<>();


    @Override
    public List<Task> getHistory() {
        Node cursor = head;
        List<Task> tasks = new ArrayList<>();
        while (cursor != null) {
            tasks.add(cursor.task);
            cursor = cursor.next;
        }
        return new ArrayList<>(tasks);
    }

    private void linkLast(Node node) {
        if (tail != null) {
            Node oldTail = tail;
            tail = node;
            tail.prev = oldTail;
            oldTail.next = tail;
        } else {
            if (head != null) {
                tail = node;
                head.next = tail;
                tail.prev = head;
            } else {
                head = node;
            }
        }
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }
        if (head == null && tail == null) {
            return;
        }
        if (head == node) {
            head = node.next;
            if (head != null) {
                head.prev = null;
            }
        } else if (tail == node) {
            tail = node.prev;
            tail.next = null;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        node.next = null;
        node.prev = null;
    }

    @Override
    public void add(Task task) {
        remove(task.getId());
        Node newNode = new Node(task);
        linkLast(newNode);
        historyMap.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        removeNode(historyMap.get(id));
        historyMap.remove(id);
    }

}
