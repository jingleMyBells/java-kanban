package ru.atlassian.jira.exceptions;

public class KVClientWrongStatusCode extends RuntimeException {

    public KVClientWrongStatusCode(String message) {
        super(message);
    }
}
