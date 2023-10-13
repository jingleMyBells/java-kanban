package ru.atlassian.jira.exceptions;

public class MessageException extends RuntimeException {
    public MessageException(String message) {
        super(message);
    }
}
