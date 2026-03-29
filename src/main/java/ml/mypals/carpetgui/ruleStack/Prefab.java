package ml.mypals.carpetgui.ruleStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

public class Prefab {

    private final String name;
    private final long createdAt;
    private final Map<String, RuleValueSnapshot> baseline;
    private final List<RuleLayer> layers;
    private int layerCounter;

    public Prefab(String name, Map<String, RuleValueSnapshot> baseline) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.baseline = new HashMap<>(baseline);
        this.layers = new ArrayList<>();
        this.layerCounter = 0;
    }

    Prefab(String name, long createdAt, Map<String, RuleValueSnapshot> baseline, List<RuleLayer> layers, int counter) {
        this.name = name;
        this.createdAt = createdAt;
        this.baseline = baseline;
        this.layers = layers;
        this.layerCounter = counter;
    }

    public static Prefab fromJson(JsonObject o) {
        Map<String, RuleValueSnapshot>
                baseline = new HashMap<>();
        o.getAsJsonObject("baseline").entrySet()
                .forEach(e -> baseline.put(e.getKey(), RuleValueSnapshot.fromJson(e.getValue().getAsJsonObject())));

        List<RuleLayer> layers = new ArrayList<>();
        o.getAsJsonArray("layers").forEach(e -> layers.add(RuleLayer.fromJson(e.getAsJsonObject())));

        return new Prefab(
                o.get("name").getAsString(),
                o.get("createdAt").getAsLong(),
                baseline, layers,
                o.get("counter").getAsInt()
        );
    }

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getSize() {
        return layers.size();
    }

    public boolean isEmpty() {
        return layers.isEmpty();
    }

    public List<RuleLayer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    public Map<String, RuleValueSnapshot> getBaseline() {
        return Collections.unmodifiableMap(baseline);
    }

    public RuleLayer peek() {
        return layers.isEmpty() ? null : layers.getLast();
    }

    public int nextId() {
        return ++layerCounter;
    }

    public void pushLayer(RuleLayer layer) {
        layers.add(layer);
    }

    public RuleLayer popLayer() {
        return layers.isEmpty() ? null : layers.removeLast();
    }

    public Map<String, RuleValueSnapshot> resolvedState() {
        Map<String, RuleValueSnapshot> state = new HashMap<>(baseline);
        for (RuleLayer layer : layers) {
            for (RuleChange c : layer.getChanges()) {
                state.put(c.ruleKey(), c.newSnapshot());
            }
        }
        return state;
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("name", name);
        o.addProperty("createdAt", createdAt);
        o.addProperty("counter", layerCounter);

        JsonObject bl = new JsonObject();
        baseline.forEach((k, v) -> bl.add(k, v.toJson()));
        o.add("baseline", bl);

        JsonArray arr = new JsonArray();
        layers.forEach(l -> arr.add(l.toJson()));
        o.add("layers", arr);
        return o;
    }
}