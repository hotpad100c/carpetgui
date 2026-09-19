package ml.mypals.carpetgui.ruleStack;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import ml.mypals.carpetgui.CarpetGUI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.permissions.Permissions;

public final class RuleStackCommand {
   private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

   private RuleStackCommand() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("rulestack").requires((src) -> src.permissions().hasPermission(Permissions.COMMANDS_ADMIN))).then(((LiteralArgumentBuilder)Commands.literal("push").executes((c) -> doPush(c, ""))).then(Commands.argument("message", StringArgumentType.greedyString()).executes((c) -> doPush(c, StringArgumentType.getString(c, "message")))))).then(Commands.literal("pop").executes(RuleStackCommand::doPop))).then(Commands.literal("status").executes(RuleStackCommand::doStatus))).then(((LiteralArgumentBuilder)Commands.literal("show").executes(RuleStackCommand::doShowTop)).then(Commands.argument("layerId", StringArgumentType.word()).executes((c) -> doShowById(c, StringArgumentType.getString(c, "layerId")))))).then(Commands.literal("diff").executes(RuleStackCommand::doDiff))).then(Commands.literal("discard").executes(RuleStackCommand::discard))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("prefab").then(Commands.literal("list").executes(RuleStackCommand::doPrefabList))).then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.word()).then(Commands.argument("forkCurrent", BoolArgumentType.bool()).executes((c) -> doPrefabCreate(c, StringArgumentType.getString(c, "name"), BoolArgumentType.getBool(c, "forkCurrent"))))))).then(Commands.literal("delete").then(Commands.argument("name", StringArgumentType.word()).suggests((c, b) -> {
         PrefabManager m = mgr();
         if (m != null) {
            m.getAllPrefabs().forEach((p) -> b.suggest(p.getName()));
         }

         return b.buildFuture();
      }).executes((c) -> doPrefabDelete(c, StringArgumentType.getString(c, "name")))))).then(Commands.literal("switch").then(Commands.argument("name", StringArgumentType.word()).suggests((c, b) -> {
         PrefabManager m = mgr();
         if (m != null) {
            m.getAllPrefabs().stream().filter((p) -> !p.getName().equals(m.getActiveName())).forEach((p) -> b.suggest(p.getName()));
         }

         return b.buildFuture();
      }).executes((c) -> doPrefabSwitch(c, StringArgumentType.getString(c, "name")))))));
   }

   private static PrefabManager mgr() {
      return CarpetGUI.getPrefabManager();
   }

   private static boolean guard(CommandContext<CommandSourceStack> ctx) {
      if (mgr() == null) {
         failure(ctx, Component.translatable("commands.rulestack.not_init"));
         return true;
      } else {
         return false;
      }
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

      MutableComponent line = Component.translatable("commands.rulestack.change.line", new Object[]{Component.literal("§e" + ruleKey), snapshotDisplay(c.previousSnapshot(), "§c"), snapshotDisplay(c.newSnapshot(), "§a")});
      if (!valueChanged && defaultChanged) {
         line.append(Component.translatable(c.newSnapshot().isDefault() ? "commands.rulestack.change.became_default" : "commands.rulestack.change.removed_default"));
      }

      return line;
   }

   private static int doPush(CommandContext<CommandSourceStack> ctx, String msg) {
      if (guard(ctx)) {
         return 0;
      } else {
         PrefabManager.PushResult result = mgr().push(msg);
         if (result == null) {
            return failure(ctx, Component.translatable("commands.rulestack.push.no_changes"));
         } else {
            RuleLayer layer = result.layer();
            String translationKey = result.wasRedo() ? "commands.rulestack.push.redo" : "commands.rulestack.push.success";
            Component response = Component.translatable(translationKey, new Object[]{String.valueOf(layer.getId()), msg.isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", new Object[]{msg}), String.valueOf(layer.getChanges().size())});
            return success(ctx, response, true);
         }
      }
   }

   private static int doPop(CommandContext<CommandSourceStack> ctx) {
      if (guard(ctx)) {
         return 0;
      } else {
         RuleLayer layer = mgr().pop();
         if (layer == null) {
            failure(ctx, Component.translatable("commands.rulestack.pop.empty"));
            return 0;
         } else {
            MutableComponent msg = Component.translatable("commands.rulestack.pop.success", new Object[]{String.valueOf(layer.getId()), layer.getMessage().isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", new Object[]{layer.getMessage()}), String.valueOf(layer.getChanges().size())});
            msg.append(Component.literal(" ")).append(Component.translatable("commands.rulestack.pop.redo_hint"));

            for(RuleChange c : layer.getChanges()) {
               msg.append(Component.literal("\n  ")).append(renderChange(c));
            }

            success(ctx, msg, true);
            return 1;
         }
      }
   }

   private static int discard(CommandContext<CommandSourceStack> ctx) {
      if (guard(ctx)) {
         return 0;
      } else {
         RuleLayer layer = mgr().popAllWithoutSave();
         if (layer == null) {
            failure(ctx, Component.translatable("commands.rulestack.pop.empty"));
            return 0;
         } else {
            MutableComponent msg = Component.translatable("commands.rulestack.pop.success", new Object[]{String.valueOf(layer.getId()), layer.getMessage().isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", new Object[]{layer.getMessage()}), String.valueOf(layer.getChanges().size())});

            for(RuleChange c : layer.getChanges()) {
               msg.append(Component.literal("\n  ")).append(renderChange(c));
            }

            success(ctx, msg, true);
            return 1;
         }
      }
   }

   private static int doStatus(CommandContext<CommandSourceStack> ctx) {
      if (guard(ctx)) {
         return 0;
      } else {
         PrefabManager m = mgr();
         Prefab active = m.getActivePrefab();
         List<RuleChange> pending = m.getPendingChanges();
         MutableComponent msg = Component.translatable("commands.rulestack.status.header", new Object[]{m.getActiveName(), String.valueOf(active.getSize())});
         if (!pending.isEmpty()) {
            msg.append(Component.literal("\n")).append(Component.translatable("commands.rulestack.status.pending_warning", new Object[]{String.valueOf(pending.size())}));
         }

         List<RuleLayer> layers = active.getLayers();
         if (layers.isEmpty() && !active.hasFuture()) {
            msg.append(Component.literal("\n")).append(Component.translatable("commands.rulestack.status.empty_stack"));
         } else {
            List<RuleLayer> future = active.getFutureLayers();
            if (!future.isEmpty()) {
               msg.append(Component.literal("\n")).append(Component.translatable("commands.rulestack.status.future_header", new Object[]{String.valueOf(future.size())}));

               for(int i = future.size() - 1; i >= 0; --i) {
                  RuleLayer l = (RuleLayer)future.get(i);
                  boolean nextRedo = i == future.size() - 1;
                  msg.append(Component.translatable("commands.rulestack.status.future_entry", new Object[]{nextRedo ? "§b►§r " : "  ", String.valueOf(l.getId()), l.getMessage().isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", new Object[]{l.getMessage()}), String.valueOf(l.getChanges().size()), ts(l.getTimestamp())}));
               }
            }

            if (!layers.isEmpty()) {
               msg.append(Component.literal("\n")).append(Component.translatable("commands.rulestack.status.layer_list_header"));

               for(int i = layers.size() - 1; i >= 0; --i) {
                  RuleLayer l = (RuleLayer)layers.get(i);
                  boolean isTop = i == layers.size() - 1;
                  msg.append(Component.translatable("commands.rulestack.status.layer_entry", new Object[]{isTop ? "§a►§r " : "  ", String.valueOf(l.getId()), l.getMessage().isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", new Object[]{l.getMessage()}), String.valueOf(l.getChanges().size()), ts(l.getTimestamp())}));
               }
            }
         }

         success(ctx, msg, true);
         return 1;
      }
   }

   private static int doShowTop(CommandContext<CommandSourceStack> ctx) {
      if (guard(ctx)) {
         return 0;
      } else {
         RuleLayer layer = mgr().getActivePrefab().peek();
         if (layer == null) {
            failure(ctx, Component.translatable("commands.rulestack.show.empty"));
            return 0;
         } else {
            return renderLayer(ctx, layer);
         }
      }
   }

   private static int doShowById(CommandContext<CommandSourceStack> ctx, String idStr) {
      if (guard(ctx)) {
         return 0;
      } else {
         int id;
         try {
            id = Integer.parseInt(idStr);
         } catch (NumberFormatException var4) {
            failure(ctx, Component.translatable("commands.rulestack.show.invalid_id", new Object[]{idStr}));
            return 0;
         }

         Optional<RuleLayer> found = mgr().getActivePrefab().getLayers().stream().filter((l) -> l.getId() == id).findFirst();
         if (found.isEmpty()) {
            failure(ctx, Component.translatable("commands.rulestack.show.not_found", new Object[]{String.valueOf(id)}));
            return 0;
         } else {
            return renderLayer(ctx, (RuleLayer)found.get());
         }
      }
   }

   private static int renderLayer(CommandContext<CommandSourceStack> ctx, RuleLayer layer) {
      MutableComponent msg = Component.translatable("commands.rulestack.layer.header", new Object[]{String.valueOf(layer.getId()), layer.getMessage().isEmpty() ? Component.literal("") : Component.translatable("commands.rulestack.push.message_suffix", new Object[]{layer.getMessage()}), ts(layer.getTimestamp()), String.valueOf(layer.getChanges().size())});

      for(RuleChange c : layer.getChanges()) {
         msg.append(Component.literal("\n  ")).append(renderChange(c));
      }

      success(ctx, msg, false);
      return 1;
   }

   private static int doDiff(CommandContext<CommandSourceStack> ctx) {
      if (guard(ctx)) {
         return 0;
      } else {
         List<RuleChange> changes = mgr().getPendingChanges();
         if (changes.isEmpty()) {
            success(ctx, Component.translatable("commands.rulestack.diff.no_changes"), false);
            return 1;
         } else {
            MutableComponent msg = Component.translatable("commands.rulestack.diff.header", new Object[]{String.valueOf(changes.size())});

            for(RuleChange c : changes) {
               msg.append(Component.literal("\n  ")).append(renderChange(c));
            }

            success(ctx, msg, false);
            return 1;
         }
      }
   }

   private static int doPrefabList(CommandContext<CommandSourceStack> ctx) {
      if (guard(ctx)) {
         return 0;
      } else {
         PrefabManager m = mgr();
         MutableComponent msg = Component.translatable("commands.rulestack.prefab.list.header");

         for(Prefab p : m.getAllPrefabs()) {
            boolean active = p.getName().equals(m.getActiveName());
            msg.append(Component.translatable("commands.rulestack.prefab.list.entry", new Object[]{active ? "§a►§r " : "  ", p.getName(), String.valueOf(p.getSize())}));
         }

         success(ctx, msg, false);
         return 1;
      }
   }

   private static int doPrefabCreate(CommandContext<CommandSourceStack> ctx, String name, boolean fork) {
      if (guard(ctx)) {
         return 0;
      } else if (mgr().hasPrefab(name)) {
         failure(ctx, Component.translatable("commands.rulestack.prefab.create.exists", new Object[]{name}));
         return 0;
      } else {
         mgr().createPrefab(name, !fork);
         success(ctx, Component.translatable("commands.rulestack.prefab.create.success", new Object[]{name}), false);
         return 1;
      }
   }

   private static int doPrefabDelete(CommandContext<CommandSourceStack> ctx, String name) {
      if (guard(ctx)) {
         return 0;
      } else if (!mgr().hasPrefab(name)) {
         failure(ctx, Component.translatable("commands.rulestack.prefab.delete.not_found", new Object[]{name}));
         return 0;
      } else if (!mgr().deletePrefab(name)) {
         failure(ctx, Component.translatable("commands.rulestack.prefab.delete.is_active"));
         return 0;
      } else {
         success(ctx, Component.translatable("commands.rulestack.prefab.delete.success", new Object[]{name}), true);
         return 1;
      }
   }

   private static int doPrefabSwitch(CommandContext<CommandSourceStack> ctx, String name) {
      if (guard(ctx)) {
         return 0;
      } else {
         byte var10000;
         switch (mgr().switchPrefab(name)) {
            case NOT_FOUND:
               failure(ctx, Component.translatable("commands.rulestack.prefab.switch.not_found", new Object[]{name}));
               var10000 = 0;
               break;
            case ALREADY_ACTIVE:
               failure(ctx, Component.translatable("commands.rulestack.prefab.switch.already_active", new Object[]{name}));
               var10000 = 0;
               break;
            case SUCCESS_DIRTY:
               success(ctx, Component.translatable("commands.rulestack.prefab.switch.success_dirty", new Object[]{name}), true);
               var10000 = 1;
               break;
            default:
               success(ctx, Component.translatable("commands.rulestack.prefab.switch.success", new Object[]{name}), true);
               var10000 = 1;
         }

         return var10000;
      }
   }

   private static int send(CommandContext<CommandSourceStack> ctx, Component component, boolean success, boolean broadcast) {
      if (success) {
         ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> component, broadcast);
      } else {
         ((CommandSourceStack)ctx.getSource()).sendFailure(component);
      }

      return success ? 1 : 0;
   }

   private static int success(CommandContext<CommandSourceStack> ctx, Component component, boolean broadcast) {
      return send(ctx, component, true, broadcast);
   }

   private static int failure(CommandContext<CommandSourceStack> ctx, Component component) {
      return send(ctx, component, false, false);
   }
}
