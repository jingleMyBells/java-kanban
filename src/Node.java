public class Node<Task> {
    public Task task;
    public Node<Task> next;
    public Node<Task> prev;

    public Node(Task task) {
        this.task = task;
        this.next = null;
        this.prev = null;
    }
}
