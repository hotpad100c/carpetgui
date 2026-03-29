package ml.mypals.carpetgui.ruleStack;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import ml.mypals.carpetgui.CarpetGUI;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class RuleStackCommand {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

    private RuleStackCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx) {
        dispatcher.register(
                literal("rulestack").requires(src -> src.hasPermission(2))
                        .then(literal("push")
                                .executes(c -> doPush(c, ""))
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(c -> doPush(c, StringArgumentType.getString(c, "message")))))
                        .then(literal("pop")
                                .executes(RuleStackCommand::doPop))
                        .then(literal("status")
                                .executes(RuleStackCommand::doStatus))
                        .then(literal("show")
                                .executes(c -> doShowTop(c))
                                .then(argument("layerId", StringArgumentType.word())
                                        .executes(c -> doShowById(c, StringArgumentType.getString(c, "layerId")))))
                        .then(literal("diff")
                                .executes(RuleStackCommand::doDiff))
                        .then(literal("prefab")
                                .then(literal("list")
                                        .executes(RuleStackCommand::doPrefabList))
                                .then(literal("create")
                                        .then(argument("name", StringArgumentType.word())
                                                .then(argument("forkCurrent", BoolArgumentType.bool())
                                                        .executes(c -> doPrefabCreate(c,
                                                                StringArgumentType.getString(c, "name"),
                                                                BoolArgumentType.getBool(c, "forkCurrent"))))))
                                .then(literal("delete")
                                        .then(argument("name", StringArgumentType.word())
                                                .suggests((c, b) -> {
                                                    PrefabManager m = mgr();
                                                    if (m != null)
                                                        m.getAllPrefabs().forEach(p -> b.suggest(p.getName()));
                                                    return b.buildFuture();
                                                })
                                                .executes(c -> doPrefabDelete(c, StringArgumentType.getString(c, "name")))))
                                .then(literal("switch")
                                        .then(argument("name", StringArgumentType.word())
                                                .suggests((c, b) -> {
                                                    PrefabManager m = mgr();
                                                    if (m != null) m.getAllPrefabs().stream()
                                                            .filter(p -> !p.getName().equals(m.getActiveName()))
                                                            .forEach(p -> b.suggest(p.getName()));
                                                    return b.buildFuture();
                                                })
                                                .executes(c -> doPrefabSwitch(c, StringArgumentType.getString(c, "name"))))))
        );
    }

    private static PrefabManager mgr() {
        return CarpetGUI.getPrefabManager();
    }

    private static boolean guard(CommandContext<CommandSourceStack> ctx) {
        if (mgr() == null) {
            ctx.getSource().sendFailure(Component.translatable("commands.rulestack.not_init"));
            return false;
        }
        return true;
    }

    private static String ts(long ms) {
        return FMT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault()));
    }

    private static Component snapshotDisplay(RuleValueSnapshot snap, String baseColor) {
        MutableComponent base = Component.literal(baseColor + snap.value());
        if (snap.isDefault()) {
            base.append(Component.translatable("commands.rulestack.change.default_marker"));
        }
        return base;
    }

    private static Component renderChange(RuleChange c) {
        boolean valueChanged = !c.previousSnapshot().value().equals(c.newSnapshot().value());
        boolean defaultChanged = c.previousSnapshot().isDefault() != c.newSnapshot().isDefault();


        String ruleKey = c.ruleKey();
        if (ruleKey.startsWith("gamerule")) {
            String[] parts = ruleKey.split("\\$");
            ruleKey = parts[0] + "$" + parts[1].split(":")[1];
        }

        MutableComponent line = Component.translatable(
                "commands.rulestack.change.line",
                Component.literal("§e" + ruleKey),
                snapshotDisplay(c.previousSnapshot(), "§c"),
                snapshotDisplay(c.newSnapshot(), "§a")
        );

        if (!valueChanged && defaultChanged) {
            line.append(Component.translatable(
                    c.newSnapshot().isDefault()
                            ? "commands.rulestack.change.became_default"
                            : "commands.rulestack.change.removed_default"
            ));
        }

        return line;
    }

    private static int doPush(CommandContext<CommandSourceStack> ctx, String msg) {
        if (!guard(ctx)) return 0;
        RuleLayer layer = mgr().push(msg);
        if (layer == null) {
            ctx.getSource().sendFailure(Component.translatable("commands.rulestack.push.no_changes"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.rulestack.push.success",
                String.valueOf(layer.getId()),
                msg.isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", msg),
                String.valueOf(layer.getChanges().size())
        ), true);
        return 1;
    }

    private static int doPop(CommandContext<CommandSourceStack> ctx) {
        if (!guard(ctx)) return 0;
        RuleLayer layer = mgr().pop();
        if (layer == null) {
            ctx.getSource().sendFailure(Component.translatable("commands.rulestack.pop.empty"));
            return 0;
        }
        MutableComponent msg = Component.translatable(
                "commands.rulestack.pop.success",
                String.valueOf(layer.getId()),
                layer.getMessage().isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", layer.getMessage()),
                String.valueOf(layer.getChanges().size())
        );
        for (RuleChange c : layer.getChanges()) {
            msg.append(Component.literal("\n  ")).append(renderChange(c));
        }
        ctx.getSource().sendSuccess(() -> msg, true);
        return 1;
    }

    private static int doStatus(CommandContext<CommandSourceStack> ctx) {
        if (!guard(ctx)) return 0;
        PrefabManager m = mgr();
        Prefab active = m.getActivePrefab();
        List<RuleChange> pending = m.getPendingChanges();

        MutableComponent msg = Component.translatable(
                "commands.rulestack.status.header",
                m.getActiveName(),
                String.valueOf(active.getSize())
        );

        if (!pending.isEmpty()) {
            msg.append(Component.literal("\n")).append(Component.translatable(
                    "commands.rulestack.status.pending_warning",
                    String.valueOf(pending.size())
            ));
        }

        List<RuleLayer> layers = active.getLayers();
        if (layers.isEmpty()) {
            msg.append(Component.literal("\n")).append(Component.translatable("commands.rulestack.status.empty_stack"));
        } else {
            msg.append(Component.literal("\n")).append(Component.translatable("commands.rulestack.status.layer_list_header"));
            for (int i = layers.size() - 1; i >= 0; i--) {
                RuleLayer l = layers.get(i);
                boolean isTop = (i == layers.size() - 1);
                msg.append(Component.translatable(
                        "commands.rulestack.status.layer_entry",
                        isTop ? "§a►§r " : "  ",
                        String.valueOf(l.getId()),
                        l.getMessage().isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", l.getMessage()),
                        String.valueOf(l.getChanges().size()),
                        ts(l.getTimestamp())
                ));
            }
        }
        ctx.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int doShowTop(CommandContext<CommandSourceStack> ctx) {
        if (!guard(ctx)) return 0;
        RuleLayer layer = mgr().getActivePrefab().peek();
        if (layer == null) {
            ctx.getSource().sendFailure(Component.translatable("commands.rulestack.show.empty"));
            return 0;
        }
        return renderLayer(ctx, layer);
    }

    private static int doShowById(CommandContext<CommandSourceStack> ctx, String idStr) {
        if (!guard(ctx)) return 0;
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            ctx.getSource().sendFailure(Component.translatable("commands.rulestack.show.invalid_id", idStr));
            return 0;
        }
        Optional<RuleLayer> found = mgr().getActivePrefab().getLayers().stream()
                .filter(l -> l.getId() == id).findFirst();
        if (found.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("commands.rulestack.show.not_found", String.valueOf(id)));
            return 0;
        }
        return renderLayer(ctx, found.get());
    }

    private static int renderLayer(CommandContext<CommandSourceStack> ctx, RuleLayer layer) {


        MutableComponent msg = Component.translatable(
                "commands.rulestack.layer.header",
                String.valueOf(layer.getId()),
                layer.getMessage().isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", layer.getMessage()),
                ts(layer.getTimestamp()),
                String.valueOf(layer.getChanges().size())
        );
        for (RuleChange c : layer.getChanges()) {

            msg.append(Component.literal("\n  ")).append(renderChange(c));
        }
        ctx.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int doDiff(CommandContext<CommandSourceStack> ctx) {
        if (!guard(ctx)) return 0;
        List<RuleChange> changes = mgr().getPendingChanges();
        if (changes.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.rulestack.diff.no_changes"), false);
            return 1;
        }
        MutableComponent msg = Component.translatable(
                "commands.rulestack.diff.header",
                String.valueOf(changes.size())
        );
        for (RuleChange c : changes) {
            msg.append(Component.literal("\n  ")).append(renderChange(c));
        }
        ctx.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int doPrefabList(CommandContext<CommandSourceStack> ctx) {
        if (!guard(ctx)) return 0;
        PrefabManager m = mgr();
        MutableComponent msg = Component.translatable("commands.rulestack.prefab.list.header");
        for (Prefab p : m.getAllPrefabs()) {
            boolean active = p.getName().equals(m.getActiveName());
            msg.append(Component.translatable(
                    "commands.rulestack.prefab.list.entry",
                    active ? "§a►§r " : "  ",
                    p.getName(),
                    String.valueOf(p.getSize())
            ));
        }
        ctx.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int doPrefabCreate(CommandContext<CommandSourceStack> ctx, String name, boolean fork) {
        if (!guard(ctx)) return 0;
        if (mgr().hasPrefab(name)) {
            ctx.getSource().sendFailure(Component.translatable("commands.rulestack.prefab.create.exists", name));
            return 0;
        }
        mgr().createPrefab(name, !fork);
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.rulestack.prefab.create.success", name), true);
        return 1;
    }

    private static int doPrefabDelete(CommandContext<CommandSourceStack> ctx, String name) {
        if (!guard(ctx)) return 0;
        if (!mgr().hasPrefab(name)) {
            ctx.getSource().sendFailure(Component.translatable("commands.rulestack.prefab.delete.not_found", name));
            return 0;
        }
        if (!mgr().deletePrefab(name)) {
            ctx.getSource().sendFailure(Component.translatable("commands.rulestack.prefab.delete.is_active"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.rulestack.prefab.delete.success", name), true);
        return 1;
    }

    private static int doPrefabSwitch(CommandContext<CommandSourceStack> ctx, String name) {
        if (!guard(ctx)) return 0;
        return switch (mgr().switchPrefab(name)) {
            case NOT_FOUND -> {
                ctx.getSource().sendFailure(Component.translatable("commands.rulestack.prefab.switch.not_found", name));
                yield 0;
            }
            case ALREADY_ACTIVE -> {
                ctx.getSource().sendFailure(Component.translatable("commands.rulestack.prefab.switch.already_active", name));
                yield 0;
            }
            case SUCCESS_DIRTY -> {
                ctx.getSource().sendSuccess(() -> Component.translatable(
                        "commands.rulestack.prefab.switch.success_dirty", name), true);
                yield 1;
            }
            default -> {
                ctx.getSource().sendSuccess(() -> Component.translatable(
                        "commands.rulestack.prefab.switch.success", name), true);
                yield 1;
            }
        };
    }
}