package ml.mypals.carpetgui.ruleStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Prefab {
   private final String name;
   private final long createdAt;
   private final Map<String, RuleValueSnapshot> baseline;
   private final List<RuleLayer> layers;
   private final List<RuleLayer> futureLayers;
   private int layerCounter;

   public Prefab(String name, Map<String, RuleValueSnapshot> baseline) {
      this.name = name;
      this.createdAt = System.currentTimeMillis();
      this.baseline = new HashMap(baseline);
      this.layers = new ArrayList();
      this.futureLayers = new ArrayList();
      this.layerCounter = 0;
   }

   Prefab(String name, long createdAt, Map<String, RuleValueSnapshot> baseline, List<RuleLayer> layers, List<RuleLayer> futureLayers, int counter) {
      this.name = name;
      this.createdAt = createdAt;
      this.baseline = baseline;
      this.layers = layers;
      this.futureLayers = futureLayers;
      this.layerCounter = counter;
   }

   public static Prefab fromJson(JsonObject o) {
      Map<String, RuleValueSnapshot> baseline = new HashMap();
      o.getAsJsonObject("baseline").entrySet().forEach((e) -> baseline.put((String)e.getKey(), RuleValueSnapshot.fromJson(((JsonElement)e.getValue()).getAsJsonObject())));
      List<RuleLayer> layers = new ArrayList();
      o.getAsJsonArray("layers").forEach((e) -> layers.add(RuleLayer.fromJson(e.getAsJsonObject())));
      List<RuleLayer> futureLayers = new ArrayList();
      if (o.has("futureLayers")) {
         o.getAsJsonArray("futureLayers").forEach((e) -> futureLayers.add(RuleLayer.fromJson(e.getAsJsonObject())));
      }

      return new Prefab(o.get("name").getAsString(), o.get("createdAt").getAsLong(), baseline, layers, futureLayers, o.get("counter").getAsInt());
   }

   public String getName() {
      return this.name;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   public int getSize() {
      return this.layers.size();
   }

   public boolean isEmpty() {
      return this.layers.isEmpty();
   }

   public List<RuleLayer> getLayers() {
      return Collections.unmodifiableList(this.layers);
   }

   public Map<String, RuleValueSnapshot> getBaseline() {
      return Collections.unmodifiableMap(this.baseline);
   }

   public RuleLayer peek() {
      return this.layers.isEmpty() ? null : (RuleLayer)this.layers.getLast();
   }

   public int nextId() {
      return ++this.layerCounter;
   }

   public void pushLayer(RuleLayer layer) {
      this.layers.add(layer);
   }

   public RuleLayer popLayer() {
      return this.layers.isEmpty() ? null : (RuleLayer)this.layers.removeLast();
   }

   public boolean hasFuture() {
      return !this.futureLayers.isEmpty();
   }

   public List<RuleLayer> getFutureLayers() {
      return Collections.unmodifiableList(this.futureLayers);
   }

   public void pushFuture(RuleLayer layer) {
      this.futureLayers.add(layer);
   }

   public RuleLayer popFuture() {
      return this.futureLayers.isEmpty() ? null : (RuleLayer)this.futureLayers.removeLast();
   }

   public void clearFuture() {
      this.futureLayers.clear();
   }

   public Map<String, RuleValueSnapshot> resolvedState() {
      Map<String, RuleValueSnapshot> state = new HashMap(this.baseline);

      for(RuleLayer layer : this.layers) {
         for(RuleChange c : layer.getChanges()) {
            state.put(c.ruleKey(), c.newSnapshot());
         }
      }

      return state;
   }

   public JsonObject toJson() {
      JsonObject o = new JsonObject();
      o.addProperty("name", this.name);
      o.addProperty("createdAt", this.createdAt);
      o.addProperty("counter", this.layerCounter);
      JsonObject bl = new JsonObject();
      this.baseline.forEach((k, v) -> bl.add(k, v.toJson()));
      o.add("baseline", bl);
      JsonArray arr = new JsonArray();
      this.layers.forEach((l) -> arr.add(l.toJson()));
      o.add("layers", arr);
      JsonArray future = new JsonArray();
      this.futureLayers.forEach((l) -> future.add(l.toJson()));
      o.add("futureLayers", future);
      return o;
   }
}
