package ru.atlassian.jira.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Objects;
import java.util.LinkedList;

import ru.atlassian.jira.model.Task;

public class InMemoryHistoryManager implements HistoryManager {

    private Node head = null;
    private Node tail = null;
    private final Map<Integer, Node> historyMap = new HashMap<>();


    @Override
    public List<Task> getHistory() {
        Node cursor = head;
        List<Task> tasks = new LinkedList<>();
        while (cursor != null) {
            tasks.add(cursor.task);
            cursor = cursor.next;
        }
        return new ArrayList<>(tasks);
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        Node newNode = new Node(task);
        linkLast(newNode);
        historyMap.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        removeNode(historyMap.remove(id));
    }

    private void linkLast(Node node) {
        if (tail != null) {
            Node oldTail = tail;
            tail = node;
            tail.prev = oldTail;
            oldTail.next = tail;
        } else {
            head = node;
            tail = node;
        }
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }
        final Node next = node.next;
        final Node prev = node.prev;
        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }
        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }

    }

    private static class Node {
        public Task task;
        public Node next;
        public Node prev;

        public Node(Task task) {
            this.task = task;
            this.next = null;
            this.prev = null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(task, node.task);
        }

        @Override
        public int hashCode() {
            return Objects.hash(task);
        }
    }


}
