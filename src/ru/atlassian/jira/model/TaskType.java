package ru.atlassian.jira.model;

public enum TaskType {
    TASK {
        @Override
        public String toString() {
            return "TASK";
        }
    },
    EPIC,
    SUBTASK
}
