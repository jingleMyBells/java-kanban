package ru.atlassian.jira.exceptions;

public class ManagerReadException extends Exception {

    public ManagerReadException() {
        super();
    }

    public ManagerReadException(String message) {
        super(message);
    }
}
