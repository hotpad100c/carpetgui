package ml.mypals.carpetgui.ruleStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class RuleLayer {

    private final int id;
    private final String message;
    private final long timestamp;
    private final List<RuleChange> changes;

    public RuleLayer(int id, String message, long timestamp, List<RuleChange> changes) {
        this.id = id;
        this.message = message == null ? "" : message;
        this.timestamp = timestamp;
        this.changes = List.copyOf(changes);
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<RuleChange> getChanges() {
        return changes;
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        o.addProperty("msg", message);
        o.addProperty("ts", timestamp);
        JsonArray arr = new JsonArray();
        changes.forEach(c -> arr.add(c.toJson()));
        o.add("changes", arr);
        return o;
    }

    public static RuleLayer fromJson(JsonObject o) {
        List<RuleChange> changes = new ArrayList<>();
        o.getAsJsonArray("changes").forEach(e -> changes.add(RuleChange.fromJson(e.getAsJsonObject())));
        return new RuleLayer(
                o.get("id").getAsInt(),
                o.get("msg").getAsString(),
                o.get("ts").getAsLong(),
                changes
        );
    }
}