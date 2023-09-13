package ru.atlassian.jira.exceptions;

public class ManagerEmptyStorageException extends Exception {

    public ManagerEmptyStorageException() {
        super();
    }

    public ManagerEmptyStorageException(String message) {
        super(message);
    }
}
