package ml.mypals.carpetgui.ruleStack;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ml.mypals.carpetgui.CarpetGUI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PrefabManager {

    private static final String SAVE_FILENAME = "rulestackaddon_prefabs.json";
    private static final String DEFAULT_PREFAB = "default";

    private final MinecraftServer server;
    private final Path saveFile;

    private String activeName;
    private final Map<String, Prefab> prefabs = new LinkedHashMap<>();

    private Map<String, RuleValueSnapshot> committedSnapshot = new LinkedHashMap<>();

    public PrefabManager(MinecraftServer server) {
        this.server = server;
        this.saveFile = server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve(SAVE_FILENAME);
    }

    public void init() {

        if (!load()) {
            committedSnapshot = SettingsWatcher.makeDefaultSnapshot();
            prefabs.put(DEFAULT_PREFAB, new Prefab(DEFAULT_PREFAB, committedSnapshot));
            activeName = DEFAULT_PREFAB;
            save();
        }
    }

    public RuleLayer push(String message) {
        Map<String, RuleValueSnapshot> live = SettingsWatcher.takeSnapshot();
        List<RuleChange> changes = diff(committedSnapshot, live);
        if (changes.isEmpty()) return null;

        Prefab active = getActivePrefab();
        RuleLayer layer = new RuleLayer(active.nextId(), message, System.currentTimeMillis(), changes);
        active.pushLayer(layer);
        committedSnapshot = new LinkedHashMap<>(live);
        save();
        return layer;
    }

    public RuleLayer pop() {
        Prefab active = getActivePrefab();
        if (active.isEmpty()) return null;

        RuleLayer layer = active.popLayer();
        CommandSourceStack src = server.createCommandSourceStack();
        for (RuleChange c : layer.getChanges()) {
            SettingsWatcher.applyRule(c.ruleKey(), c.previousSnapshot(), src);
        }
        committedSnapshot = SettingsWatcher.takeSnapshot();
        save();
        return layer;
    }


    public Prefab createPrefab(String name, boolean newOne) {
        Prefab p = new Prefab(name, newOne ? SettingsWatcher.makeDefaultSnapshot() : SettingsWatcher.takeSnapshot());
        prefabs.put(name, p);
        save();
        return p;
    }

    public boolean deletePrefab(String name) {
        if (name.equals(activeName) || !prefabs.containsKey(name)) return false;
        prefabs.remove(name);
        save();
        return true;
    }

    public SwitchResult switchPrefab(String targetName) {
        if (!prefabs.containsKey(targetName)) return SwitchResult.NOT_FOUND;
        if (targetName.equals(activeName)) return SwitchResult.ALREADY_ACTIVE;

        Map<String, RuleValueSnapshot> live = SettingsWatcher.takeSnapshot();
        boolean dirty = !diff(committedSnapshot, live).isEmpty();

        Map<String, RuleValueSnapshot> target = prefabs.get(targetName).resolvedState();
        CommandSourceStack src = server.createCommandSourceStack();
        target.forEach((key, snap) -> {
            RuleValueSnapshot current = live.get(key);
            if (!snap.equals(current)) {
                SettingsWatcher.applyRule(key, snap, src);
            }
        });

        activeName = targetName;
        committedSnapshot = SettingsWatcher.takeSnapshot();
        save();
        return dirty ? SwitchResult.SUCCESS_DIRTY : SwitchResult.SUCCESS;
    }

    public List<RuleChange> getPendingChanges() {
        return diff(committedSnapshot, SettingsWatcher.takeSnapshot());
    }

    public Prefab getActivePrefab() {
        return prefabs.get(activeName);
    }

    public String getActiveName() {
        return activeName;
    }

    public Collection<Prefab> getAllPrefabs() {
        return Collections.unmodifiableCollection(prefabs.values());
    }

    public Prefab getPrefab(String n) {
        return prefabs.get(n);
    }

    public boolean hasPrefab(String n) {
        return prefabs.containsKey(n);
    }


    private List<RuleChange> diff(Map<String, RuleValueSnapshot> base, Map<String, RuleValueSnapshot> current) {
        List<RuleChange> changes = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(base.keySet());
        keys.addAll(current.keySet());
        for (String key : keys) {
            RuleValueSnapshot before = base.getOrDefault(key, new RuleValueSnapshot("", false));
            RuleValueSnapshot after = current.getOrDefault(key, new RuleValueSnapshot("", false));
            if (!before.equals(after)) {
                changes.add(new RuleChange(key, before, after));
            }
        }
        return changes;
    }

    private void save() {
        try {
            Files.createDirectories(saveFile.getParent());

            JsonObject root = new JsonObject();
            root.addProperty("active", activeName);

            JsonObject snap = new JsonObject();
            committedSnapshot.forEach((k, v) -> snap.add(k, v.toJson()));
            root.add("committedSnapshot", snap);

            JsonArray arr = new JsonArray();
            prefabs.values().forEach(p -> arr.add(p.toJson()));
            root.add("prefabs", arr);

            Files.writeString(saveFile, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (IOException e) {
            CarpetGUI.LOGGER.error("[RuleStack] Save failed: {}", e.getMessage());
        }
    }

    private boolean load() {
        if (!Files.exists(saveFile)) return false;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(saveFile)).getAsJsonObject();
            activeName = root.get("active").getAsString();

            committedSnapshot = new LinkedHashMap<>();
            root.getAsJsonObject("committedSnapshot").entrySet()
                    .forEach(e -> committedSnapshot.put(
                            e.getKey(),
                            RuleValueSnapshot.fromJson(e.getValue().getAsJsonObject())));

            prefabs.clear();
            root.getAsJsonArray("prefabs").forEach(el -> {
                Prefab p = Prefab.fromJson(el.getAsJsonObject());
                prefabs.put(p.getName(), p);
            });
            return true;
        } catch (Exception e) {
            CarpetGUI.LOGGER.error("[RuleStack] Load failed: {}", e.getMessage());
            return false;
        }
    }

    public enum SwitchResult {SUCCESS, SUCCESS_DIRTY, NOT_FOUND, ALREADY_ACTIVE}
}