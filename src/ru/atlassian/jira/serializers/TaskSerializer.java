package ru.atlassian.jira.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import ru.atlassian.jira.model.Task;

import java.lang.reflect.Type;

public class TaskSerializer implements JsonSerializer<Task> {
    @Override
    public JsonElement serialize(Task task, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject serializedTask = new JsonObject();
        serializedTask.addProperty("id", task.getId());
        serializedTask.addProperty("title", task.getTitle());
        serializedTask.addProperty("description", task.getDescription());
        serializedTask.addProperty("status", task.getStatus().toString());
        if (task.getDuration().isPresent()) {
            serializedTask.addProperty("duration", task.getDuration().get());
        }
        if (task.getStartTime().isPresent()) {
            String time = task.getStartTime().get().format(Task.FORMATTER);
            serializedTask.addProperty("startTime", time);
        }
        return serializedTask;
    }
}
