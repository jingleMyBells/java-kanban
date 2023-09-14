package ru.atlassian.jira.exceptions;

public class ManagerEmptyStorageException extends RuntimeException {

    public ManagerEmptyStorageException(String message) {
        super(message);
    }
}
