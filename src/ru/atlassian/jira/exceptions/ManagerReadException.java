package ru.atlassian.jira.exceptions;

public class ManagerReadException extends RuntimeException {
    public ManagerReadException(String message) {
        super(message);
    }
}
