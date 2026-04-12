package ml.mypals.carpetgui.ruleStack;


import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import ml.mypals.carpetgui.CarpetGUI;
import ml.mypals.carpetgui.accessors.CommandSourceStackAccessor;
import ml.mypals.carpetgui.mixin.accessors.GameRulesAccessor;
import ml.mypals.carpetgui.mixin.accessors.SettngsManagerAccessor;
import ml.mypals.carpetgui.settings.GamerulesDefaultValueSorter;
import net.minecraft.commands.CommandSourceStack;
//? if <1.21.11 {
/*import net.minecraft.world.level.GameRules;
*///?} else {
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;import net.minecraft.world.level.gamerules.GameRules;
//?}
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static ml.mypals.carpetgui.CarpetGUI.*;
import static ml.mypals.carpetgui.CarpetGUI.getCarpetDefaultsConfigFile;


public final class SettingsWatcher {

    private SettingsWatcher() {
    }


    public static Map<String, RuleValueSnapshot> takeSnapshot() {
        Map<String, Set<String>> defaultedRules = readAllDefaults();

        Map<String, RuleValueSnapshot> snap = new LinkedHashMap<>();
        CarpetGUI.forEachCarpetManager(mgr -> {
            String id = mgr.identifier();
            Set<String> defaults = defaultedRules.getOrDefault(id, Set.of());
            for (CarpetRule<?> rule : mgr.getCarpetRules()) {
                String key = id + ":" + rule.name();
                String val = String.valueOf(rule.value());
                boolean isDefault = defaults.contains(rule.name());
                snap.put(key, new RuleValueSnapshot(val, isDefault));
            }
        });

        //? if <1.21.11 {
        /*((GameRulesAccessor) getGamerules()).carpetGUI$getRules().forEach(
                (k, v) -> {
                    String key = "gamerule$" + k.getCategory().getDescriptionId() + ":" + k.getId();
                    String val = v.toString();
                    snap.put(key, new RuleValueSnapshot(val, false));
                }
        );
        *///?} else {
        GameRules gameRules = getGamerules();
        gameRules.availableRules().toList().forEach(
                (rule) -> {
                    String key = "gamerule$" + rule.category() + ":" + rule.id();
                    String val = gameRules.get(rule).toString().toLowerCase();
                    snap.put(key, new RuleValueSnapshot(val, false));
                }
        );
        //?}

        return snap;
    }

    private static Map<String, Set<String>> readAllDefaults() {
        Map<String, Set<String>> result = new HashMap<>();
        CarpetGUI.forEachCarpetManager(mgr -> {
            Set<String> names = new HashSet<>();

            forEachCarpetManager(settingsManager -> names.addAll(readDefaultSettingsFromConf(getCarpetDefaultsConfigFile(settingsManager))));
            names.addAll(readDefaultSettingsFromOrgConf());

            result.put(mgr.identifier(), names);
        });
        return result;
    }


    public static Map<String, RuleValueSnapshot> makeDefaultSnapshot() {
        Map<String, RuleValueSnapshot> snap = new LinkedHashMap<>();
        CarpetGUI.forEachCarpetManager(mgr -> {
            String id = mgr.identifier();
            for (CarpetRule<?> rule : mgr.getCarpetRules()) {
                snap.put(id + ":" + rule.name(),
                        new RuleValueSnapshot(rule.defaultValue().toString().toLowerCase(), false));
            }
        });

        //? if <1.21.11 {
        /*GamerulesDefaultValueSorter.gamerulesDefaultValues.forEach(
                (k, v) -> {
                    String key = "gamerule$" + k.getCategory().getDescriptionId() + ":" + k.getId();
                    snap.put(key, new RuleValueSnapshot(v, false));
                }
        );
        *///?} else {
        GameRules gameRules = getGamerules();
        gameRules.availableRules().toList().forEach(
                (rule) -> {
                    String key = "gamerule$" + rule.category() + ":" + rule.id();
                    snap.put(key, new RuleValueSnapshot(rule.defaultValue().toString(), false));
                }
        );
        //?}
        return snap;
    }

    public static void applyRule(String ruleKey, RuleValueSnapshot snapshot, CommandSourceStack source) {
        boolean silentOrg = ((CommandSourceStackAccessor) source).carpetGUI$getSilent();
        ((CommandSourceStackAccessor) source).carpetGUI$setSilent(true);
        //? if <1.21.11 {
        /*int sep = ruleKey.indexOf(':');
        *///?} else {
        int sep = ruleKey.indexOf("]:")+1;//identify gamerules
        //?}
        if (sep < 0) return;
        String managerId;
        String gameruleId = ruleKey.substring(0, sep);
        if(gameruleId.isEmpty()){
            sep = ruleKey.indexOf(":");
            managerId = ruleKey.substring(0, sep);
        }else {
            managerId = gameruleId;
        }
        String ruleName = ruleKey.substring(sep + 1);

        if (managerId.startsWith("gamerule")) {
            //? if <1.21.11 {
            /*GameRules gameRules = source.getServer().getGameRules();
            GameRules.Key<?> key = findRuleKey(ruleName, gameRules);

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
            *///?} else {
            GameRules gameRules = getGamerules();
            GameRule<?> rule = findRule(ruleName, gameRules);
            try{
                if(rule.gameRuleType() == GameRuleType.BOOL){
                    gameRules.set((GameRule<Boolean>)rule, Boolean.parseBoolean(snapshot.value()), CarpetServer.minecraft_server);
                }else if(rule.gameRuleType() == GameRuleType.INT){
                    gameRules.set((GameRule<Integer>)rule, Integer.parseInt(snapshot.value()), CarpetServer.minecraft_server);
                }
            }catch (Exception ignored){}
            //?}
        } else {
            CarpetGUI.forEachCarpetManager(mgr -> {
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

    //? if <1.21.11 {
    /*private static GameRules.Key<?> findRuleKey(String name, GameRules gameRules) {
        final GameRules.Key<?>[] result = new GameRules.Key<?>[1];

        gameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.@NotNull Key<T> key, GameRules.@NotNull Type<T> type) {
                if (key.getId().equals(name)) {
                    result[0] = key;
                }
            }
        });

        return result[0];
    }
    *///?} else {
    private static GameRule<?> findRule(String name, GameRules gameRules) {
        final GameRule<?>[] result = new GameRule[1];

        gameRules.visitGameRuleTypes(new  GameRuleTypeVisitor() {
            @Override
            public <T> void visit(@NotNull GameRule<T> gameRule) {
                String path = gameRule.getIdentifier().getPath();
                if (path.equals(name)) {
                    result[0] = gameRule;
                }
            }
        });

        return result[0];
    }

    //?}
}