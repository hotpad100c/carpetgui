package ml.mypals.carpetgui.ruleStack;


import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import ml.mypals.carpetgui.accessors.CommandSourceStackAccessor;
import ml.mypals.carpetgui.mixin.accessors.GameRulesAccessor;
import ml.mypals.carpetgui.mixin.accessors.SettngsManagerAccessor;
import ml.mypals.carpetgui.settings.GamerulesDefaultValueSorter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static ml.mypals.carpetgui.CarpetGUI.getCarpetDefaultsConfigFile;
import static ml.mypals.carpetgui.CarpetGUI.readDefaultSettingsFromConf;


public final class SettingsWatcher {

    private SettingsWatcher() {
    }


    public static Map<String, RuleValueSnapshot> takeSnapshot() {
        Map<String, Set<String>> defaultedRules = readAllDefaults();

        Map<String, RuleValueSnapshot> snap = new LinkedHashMap<>();
        CarpetServer.forEachManager(mgr -> {
            String id = mgr.identifier();
            Set<String> defaults = defaultedRules.getOrDefault(id, Set.of());
            for (CarpetRule<?> rule : mgr.getCarpetRules()) {
                String key = id + ":" + rule.name();
                String val = String.valueOf(rule.value());
                boolean isDefault = defaults.contains(rule.name());
                snap.put(key, new RuleValueSnapshot(val, isDefault));
            }
        });

        ((GameRulesAccessor) CarpetServer.minecraft_server.getGameRules()).carpetGUI$getRules().forEach(
                (k, v) -> {
                    String key = "gamerule$" + k.getCategory().getDescriptionId() + ":" + k.getId();
                    String val = v.toString();
                    snap.put(key, new RuleValueSnapshot(val, false));
                }
        );
        return snap;
    }

    private static Map<String, Set<String>> readAllDefaults() {
        Map<String, Set<String>> result = new HashMap<>();
        CarpetServer.forEachManager(mgr -> {
            Set<String> names = new HashSet<>(readDefaultSettingsFromConf(getCarpetDefaultsConfigFile()));
            result.put(mgr.identifier(), names);
        });
        return result;
    }

    public static Map<String, RuleValueSnapshot> makeDefaultSnapshot() {
        Map<String, RuleValueSnapshot> snap = new LinkedHashMap<>();
        CarpetServer.forEachManager(mgr -> {
            String id = mgr.identifier();
            for (CarpetRule<?> rule : mgr.getCarpetRules()) {
                snap.put(id + ":" + rule.name(),
                        new RuleValueSnapshot(String.valueOf(rule.defaultValue()), false));
            }
        });

        GamerulesDefaultValueSorter.gamerulesDefaultValues.forEach(
                (k, v) -> {
                    String key = "gamerule$" + k.getCategory().getDescriptionId() + ":" + k.getId();
                    snap.put(key, new RuleValueSnapshot(v, false));
                }
        );
        return snap;
    }

    public static void applyRule(String ruleKey, RuleValueSnapshot snapshot, CommandSourceStack source) {
        boolean silentOrg = source.isSilent();
        ((CommandSourceStackAccessor) source).carpetGUI$setSilent(true);
        int sep = ruleKey.indexOf(':');
        if (sep < 0) return;
        String managerId = ruleKey.substring(0, sep);
        String ruleName = ruleKey.substring(sep + 1);

        if (managerId.startsWith("gamerule")) {
            GameRules gameRules = source.getServer().getGameRules();
            GameRules.Key<?> key = findRuleKey(ruleName);

            if (key == null) return;

            GameRules.Value<?> value = gameRules.getRule((GameRules.Key<?>) key);

            try {
                if (value instanceof GameRules.BooleanValue booleanValue) {
                    boolean sv = Boolean.parseBoolean(snapshot.value());
                    if (!(booleanValue.get() == sv)) {
                        booleanValue.set(sv, source.getServer());
                    }
                } else if (value instanceof GameRules.IntegerValue integerValue) {
                    int sv = Integer.parseInt(snapshot.value());
                    if (!(integerValue.get() == sv)) {
                        integerValue.set(sv, source.getServer());
                    }
                }
            } catch (Exception ignored) {
            }
        } else {
            CarpetServer.forEachManager(mgr -> {
                if (!mgr.identifier().equals(managerId)) return;
                CarpetRule<?> rule = mgr.getCarpetRule(ruleName);
                if (rule == null) return;
                try {
                    if (snapshot.isDefault()) {
                        ((SettngsManagerAccessor) mgr).carpetGUI$setDefault(source, rule, snapshot.value());
                    } else {
                        ((SettngsManagerAccessor) mgr).carpetGUI$removeDefault(source, rule);
                    }
                    ((SettngsManagerAccessor) mgr).carpetGUI$setRule(source, rule, snapshot.value());
                } catch (Exception ignored) {
                }
            });
        }
        ((CommandSourceStackAccessor) source).carpetGUI$setSilent(silentOrg);
    }

    private static GameRules.Key<?> findRuleKey(String name) {
        final GameRules.Key<?>[] result = new GameRules.Key<?>[1];

        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.@NotNull Key<T> key, GameRules.@NotNull Type<T> type) {
                if (key.getId().equals(name)) {
                    result[0] = key;
                }
            }
        });

        return result[0];
    }
}