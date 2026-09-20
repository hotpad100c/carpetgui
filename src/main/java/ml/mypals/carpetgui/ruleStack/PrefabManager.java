package ml.mypals.carpetgui.ruleStack;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ml.mypals.carpetgui.CarpetGUI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public class PrefabManager {
   private static final String SAVE_FILENAME = "rulestackaddon_prefabs.json";
   private static final String DEFAULT_PREFAB = "default";
   private final MinecraftServer server;
   private final Path saveFile;
   private String activeName;
   private final Map<String, Prefab> prefabs = new LinkedHashMap();
   private Map<String, RuleValueSnapshot> committedSnapshot = new LinkedHashMap();

   public PrefabManager(MinecraftServer server) {
      this.server = server;
      this.saveFile = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("rulestackaddon_prefabs.json");
   }

   public void init() {
      if (!this.load()) {
         this.committedSnapshot = SettingsWatcher.makeDefaultSnapshot();
         this.prefabs.put("default", new Prefab("default", this.committedSnapshot));
         this.activeName = "default";
         this.save();
      }

   }

   public PushResult push(String message) {
      Map<String, RuleValueSnapshot> live = SettingsWatcher.takeSnapshot();
      List<RuleChange> changes = this.diff(this.committedSnapshot, live);
      Prefab active = this.getActivePrefab();
      if (!changes.isEmpty()) {
         active.clearFuture();
         RuleLayer layer = new RuleLayer(active.nextId(), message, System.currentTimeMillis(), changes);
         active.pushLayer(layer);
         this.committedSnapshot = new LinkedHashMap(live);
         this.save();
         return new PushResult(layer, false);
      } else if (!active.hasFuture()) {
         return null;
      } else {
         RuleLayer future = active.popFuture();
         CommandSourceStack src = this.server.createCommandSourceStack();

         for(RuleChange c : future.getChanges()) {
            SettingsWatcher.applyRule(c.ruleKey(), c.newSnapshot(), src);
         }

         active.pushLayer(future);
         this.committedSnapshot = SettingsWatcher.takeSnapshot();
         this.save();
         return new PushResult(future, true);
      }
   }

   public RuleLayer pop() {
      Prefab active = this.getActivePrefab();
      if (active.isEmpty()) {
         return null;
      } else {
         RuleLayer layer = active.popLayer();
         active.pushFuture(layer);
         CommandSourceStack src = this.server.createCommandSourceStack();

         for(RuleChange c : layer.getChanges()) {
            SettingsWatcher.applyRule(c.ruleKey(), c.previousSnapshot(), src);
         }

         this.committedSnapshot = SettingsWatcher.takeSnapshot();
         this.save();
         return layer;
      }
   }

   public RuleLayer popAllWithoutSave() {
      Prefab active = this.getActivePrefab();
      CommandSourceStack src = this.server.createCommandSourceStack();
      List<RuleChange> pending = this.getPendingChanges();
      if (!pending.isEmpty()) {
         for(RuleChange c : pending) {
            SettingsWatcher.applyRule(c.ruleKey(), c.previousSnapshot(), src);
         }

         this.committedSnapshot = SettingsWatcher.takeSnapshot();
         this.save();
         return new RuleLayer(-1, "", -1L, pending);
      } else if (active.isEmpty()) {
         return null;
      } else {
         RuleLayer layer = active.popLayer();

         for(RuleChange c : layer.getChanges()) {
            SettingsWatcher.applyRule(c.ruleKey(), c.previousSnapshot(), src);
         }

         this.committedSnapshot = SettingsWatcher.takeSnapshot();
         this.save();
         return layer;
      }
   }

   public Prefab createPrefab(String name, boolean newOne) {
      Prefab p = new Prefab(name, newOne ? SettingsWatcher.makeDefaultSnapshot() : SettingsWatcher.takeSnapshot());
      this.prefabs.put(name, p);
      this.save();
      return p;
   }

   public boolean deletePrefab(String name) {
      if (!name.equals(this.activeName) && this.prefabs.containsKey(name)) {
         this.prefabs.remove(name);
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public SwitchResult switchPrefab(String targetName) {
      if (!this.prefabs.containsKey(targetName)) {
         return PrefabManager.SwitchResult.NOT_FOUND;
      } else if (targetName.equals(this.activeName)) {
         return PrefabManager.SwitchResult.ALREADY_ACTIVE;
      } else {
         Map<String, RuleValueSnapshot> live = SettingsWatcher.takeSnapshot();
         boolean dirty = !this.diff(this.committedSnapshot, live).isEmpty();
         Map<String, RuleValueSnapshot> target = ((Prefab)this.prefabs.get(targetName)).resolvedState();
         CommandSourceStack src = this.server.createCommandSourceStack();
         target.forEach((key, snap) -> {
            RuleValueSnapshot current = (RuleValueSnapshot)live.get(key);
            if (!snap.equals(current)) {
               SettingsWatcher.applyRule(key, snap, src);
            }

         });
         this.activeName = targetName;
         this.committedSnapshot = SettingsWatcher.takeSnapshot();
         this.save();
         return dirty ? PrefabManager.SwitchResult.SUCCESS_DIRTY : PrefabManager.SwitchResult.SUCCESS;
      }
   }

   public List<RuleChange> getPendingChanges() {
      return this.diff(this.committedSnapshot, SettingsWatcher.takeSnapshot());
   }

   public Prefab getActivePrefab() {
      return (Prefab)this.prefabs.get(this.activeName);
   }

   public String getActiveName() {
      return this.activeName;
   }

   public Collection<Prefab> getAllPrefabs() {
      return Collections.unmodifiableCollection(this.prefabs.values());
   }

   public Prefab getPrefab(String n) {
      return (Prefab)this.prefabs.get(n);
   }

   public boolean hasPrefab(String n) {
      return this.prefabs.containsKey(n);
   }

   private List<RuleChange> diff(Map<String, RuleValueSnapshot> base, Map<String, RuleValueSnapshot> current) {
      List<RuleChange> changes = new ArrayList();
      Set<String> keys = new LinkedHashSet();
      keys.addAll(base.keySet());
      keys.addAll(current.keySet());

      for(String key : keys) {
         RuleValueSnapshot before = (RuleValueSnapshot)base.getOrDefault(key, new RuleValueSnapshot("", false));
         RuleValueSnapshot after = (RuleValueSnapshot)current.getOrDefault(key, new RuleValueSnapshot("", false));
         if (!before.equals(after)) {
            changes.add(new RuleChange(key, before, after));
         }
      }

      return changes;
   }

   private void save() {
      try {
         Files.createDirectories(this.saveFile.getParent());
         JsonObject root = new JsonObject();
         root.addProperty("active", this.activeName);
         JsonObject snap = new JsonObject();
         this.committedSnapshot.forEach((k, v) -> snap.add(k, v.toJson()));
         root.add("committedSnapshot", snap);
         JsonArray arr = new JsonArray();
         this.prefabs.values().forEach((p) -> arr.add(p.toJson()));
         root.add("prefabs", arr);
         Files.writeString(this.saveFile, (new GsonBuilder()).setPrettyPrinting().create().toJson(root));
      } catch (IOException e) {
         CarpetGUI.LOGGER.error("[RuleStack] Save failed: {}", e.getMessage());
      }

   }

   private boolean load() {
      if (!Files.exists(this.saveFile, new LinkOption[0])) {
         return false;
      } else {
         try {
            //?if<=1.17.1{
            JsonParser jsonParser = new JsonParser();
            JsonObject root = jsonParser.parse(Files.readString(this.saveFile)).getAsJsonObject();
            //?}else{
            /*JsonObject root = JsonParser.parseString(Files.readString(this.saveFile)).getAsJsonObject();
             *///?}

            this.activeName = root.get("active").getAsString();
            this.committedSnapshot = new LinkedHashMap();
            root.getAsJsonObject("committedSnapshot").entrySet().forEach((ex) -> this.committedSnapshot.put((String)ex.getKey(), RuleValueSnapshot.fromJson(((JsonElement)ex.getValue()).getAsJsonObject())));
            this.prefabs.clear();
            root.getAsJsonArray("prefabs").forEach((el) -> {
               Prefab p = Prefab.fromJson(el.getAsJsonObject());
               this.prefabs.put(p.getName(), p);
            });
            return true;
         } catch (Exception e) {
            CarpetGUI.LOGGER.error("[RuleStack] Load failed: {}", e.getMessage());
            return false;
         }
      }
   }

   public static record PushResult(RuleLayer layer, boolean wasRedo) {
   }

   public static enum SwitchResult {
      SUCCESS,
      SUCCESS_DIRTY,
      NOT_FOUND,
      ALREADY_ACTIVE;

      private static SwitchResult[] $values() {
         return new SwitchResult[]{SUCCESS, SUCCESS_DIRTY, NOT_FOUND, ALREADY_ACTIVE};
      }
   }
}
