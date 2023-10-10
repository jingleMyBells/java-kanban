package ru.atlassian.jira.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonArray;
import java.lang.reflect.Type;
import java.util.List;
import ru.atlassian.jira.model.Epic;
import ru.atlassian.jira.model.Task;
import ru.atlassian.jira.model.Subtask;


public class EpicSerializer implements JsonSerializer<Epic> {
    @Override
    public JsonElement serialize(Epic epic, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject serializedEpic = new JsonObject();
        serializedEpic.addProperty("id", epic.getId());
        serializedEpic.addProperty("title", epic.getTitle());
        serializedEpic.addProperty("description", epic.getDescription());
        serializedEpic.addProperty("status", epic.getStatus().toString());
        if (epic.getDuration().isPresent()) {
            serializedEpic.addProperty("duration", epic.getDuration().get());
        }
        if (epic.getStartTime().isPresent()) {
            String time = epic.getStartTime().get().format(Task.FORMATTER);
            serializedEpic.addProperty("startTime", time);
        }
        List<Subtask> subtasks = epic.getTasks();
        JsonArray serializedSubtasks = new JsonArray();
        if (!subtasks.isEmpty()) {
            for (Subtask subtask : subtasks) {
                JsonElement serializedSubtask = new SubtaskSerializer().serialize(
                        subtask, type, jsonSerializationContext
                );
                serializedSubtasks.add(serializedSubtask);
            }
        }
        serializedEpic.add("tasks", serializedSubtasks);
        return serializedEpic;
    }
}
