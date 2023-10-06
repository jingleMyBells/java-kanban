package ru.atlassian.jira.exceptions;

public class KVClientNoTokenAvailable extends RuntimeException {

    public KVClientNoTokenAvailable(String message) {
        super(message);
    }
}
