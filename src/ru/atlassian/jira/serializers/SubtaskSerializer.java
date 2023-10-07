package ru.atlassian.jira.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import ru.atlassian.jira.model.Subtask;
import ru.atlassian.jira.model.Task;
import java.lang.reflect.Type;

public class SubtaskSerializer implements JsonSerializer<Subtask> {
    @Override
    public JsonElement serialize(Subtask subtask, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject serializedSubtask = new JsonObject();
        serializedSubtask.addProperty("id", subtask.getId());
        serializedSubtask.addProperty("title", subtask.getTitle());
        serializedSubtask.addProperty("description", subtask.getDescription());
        serializedSubtask.addProperty("status", subtask.getStatus().toString());
        serializedSubtask.addProperty("epicId", subtask.getEpicId());
        if (subtask.getDuration().isPresent()) {
            serializedSubtask.addProperty("duration", subtask.getDuration().get());
        }
        if (subtask.getStartTime().isPresent()) {
            String time = subtask.getStartTime().get().format(Task.FORMATTER);
            serializedSubtask.addProperty("startTime", time);
        }
        return serializedSubtask;
    }
}
