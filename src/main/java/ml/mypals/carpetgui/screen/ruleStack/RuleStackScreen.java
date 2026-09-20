package ml.mypals.carpetgui.screen.ruleStack;

import com.mojang.blaze3d.platform.InputConstants;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.client.CarpetGUIClientPacketHandler;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;
import ml.mypals.carpetgui.screen.ScreenTabBar;
import ml.mypals.carpetgui.screen.ScreenUtils;
import ml.mypals.carpetgui.ui.base.BaseOwoScreen;
import ml.mypals.carpetgui.ui.component.LabelComponent;
import ml.mypals.carpetgui.ui.component.TextBoxComponent;
import ml.mypals.carpetgui.ui.component.UIComponents;
import ml.mypals.carpetgui.ui.container.FlowLayout;
import ml.mypals.carpetgui.ui.container.ScrollContainer;
import ml.mypals.carpetgui.ui.container.UIContainers;
import ml.mypals.carpetgui.ui.core.Color;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.HorizontalAlignment;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIAdapter;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.core.VerticalAlignment;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

public class RuleStackScreen extends BaseOwoScreen<FlowLayout> {
   private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");
   public static RuleStackScreen INSTANCE = null;
   private Integer selectedLayerId = null;
   private int pendingRefreshTicks = 0;
   private PrefabPanel prefabPanel;
   private LabelComponent prefabNameLabel;
   private FlowLayout prefabDynamic;
   private FlowLayout timelineLayout;
   private FlowLayout changesLayout;
   private FlowLayout bottomButtonLayout;
   private LabelComponent changesHeaderLabel;
   private TextBoxComponent pushMessageBox;

   public RuleStackScreen() {
      this.prefabPanel = RuleStackScreen.PrefabPanel.NONE;
   }

   private static String ts(long ms) {
      return FMT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault()));
   }

   public void tick() {
      super.tick();
      if (this.pendingRefreshTicks > 0 && --this.pendingRefreshTicks == 0) {
         this.requestSync();
      }

   }

   protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
      return OwoUIAdapter.<FlowLayout>create(this, UIContainers::verticalFlow);
   }

   protected void build(FlowLayout root) {
      ScreenTabBar.build(this.buildMain(root), ScreenTabBar.Tab.STACK);
   }

   protected FlowLayout buildMain(FlowLayout root) {
      INSTANCE = this;
      Map.Entry<FlowLayout, FlowLayout> master = ScreenUtils.makeMasterContainer(this.width, this.height, root);
      ((FlowLayout)master.getValue()).child(this.buildLeftPanel());
      ((FlowLayout)master.getValue()).child(this.buildRightPanel());
      root.child((UIComponent)master.getKey());
      this.requestSync();
      return (FlowLayout)master.getKey();
   }

   private FlowLayout buildLeftPanel() {
      FlowLayout panel = UIContainers.verticalFlow(Sizing.fill(66), Sizing.content());
      panel.allowOverflow();
      this.changesLayout = UIContainers.verticalFlow(Sizing.fill(99), Sizing.content());
      ScrollContainer<FlowLayout> scroll = UIContainers.<FlowLayout>verticalScroll(Sizing.fill(100), Sizing.fill(100), this.changesLayout);
      scroll.surface(Surface.outline(1711276032));
      scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
      scroll.padding(Insets.of(2, 2, 2, 2));
      panel.child(scroll);
      return panel;
   }

   private FlowLayout buildRightPanel() {
      FlowLayout panel = UIContainers.verticalFlow(Sizing.fill(34), Sizing.fill(100));
      panel.surface(Surface.outline(1711276032));
      panel.padding(Insets.of(2));
      FlowLayout prefabSection = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
      prefabSection.carpetGUI$margins(Insets.bottom(5));
      panel.child(prefabSection);
      this.prefabNameLabel = UIComponents.label(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.prefab", new Object[]{"…"}).withStyle(ChatFormatting.YELLOW));
      this.prefabNameLabel.color(Color.WHITE);
      this.prefabNameLabel.carpetGUI$margins(Insets.bottom(3));
      prefabSection.child(this.prefabNameLabel);
      FlowLayout prefabBtns = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fill(5));
      prefabBtns.gap(3);
      prefabBtns.child(ScreenUtils.btn(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.btn.switch"), Sizing.fill(20), Sizing.fill(100), () -> this.togglePrefabPanel(RuleStackScreen.PrefabPanel.LIST)));
      prefabBtns.child(ScreenUtils.btn(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.btn.new_prefab"), Sizing.fill(20), Sizing.fill(100), () -> this.togglePrefabPanel(RuleStackScreen.PrefabPanel.NEW_INPUT)));
      prefabSection.child(prefabBtns);
      this.prefabDynamic = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
      this.prefabDynamic.carpetGUI$margins(Insets.top(3));
      this.prefabDynamic.horizontalAlignment(HorizontalAlignment.CENTER);
      this.prefabDynamic.verticalAlignment(VerticalAlignment.CENTER);
      prefabSection.child(this.prefabDynamic);
      this.timelineLayout = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
      ScrollContainer<FlowLayout> timelineScroll = UIContainers.<FlowLayout>verticalScroll(Sizing.fill(100), Sizing.fill(60), this.timelineLayout);
      timelineScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
      panel.child(timelineScroll);
      String hint = new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.message_hint").getString();
      this.pushMessageBox = UIComponents.textBox(Sizing.fill(100));
      this.pushMessageBox.setMaxLength(100);
      this.pushMessageBox.setSuggestion(hint);
      this.pushMessageBox.carpetGUI$focusGained().subscribe((FocusGained)(s) -> this.pushMessageBox.setSuggestion(""));
      this.pushMessageBox.carpetGUI$focusLost().subscribe((FocusLost)() -> {
         if (this.pushMessageBox.getValue().isEmpty()) {
            this.pushMessageBox.setSuggestion(hint);
         }

      });
      this.pushMessageBox.carpetGUI$margins(Insets.top(4));
      panel.child(this.pushMessageBox);
      this.bottomButtonLayout = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
      this.bottomButtonLayout.gap(4);
      this.bottomButtonLayout.carpetGUI$margins(Insets.top(2));
      this.bottomButtonLayout.horizontalAlignment(HorizontalAlignment.CENTER);
      this.buildBottomButtons();
      panel.child(this.bottomButtonLayout);
      return panel;
   }

   private void buildBottomButtons() {
      this.bottomButtonLayout.clearChildren();
      Component pushTooltip = this.rebuildPushHint();
      FlowLayout pushButton = ScreenUtils.btn(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.btn.push"), Sizing.fill(31), Sizing.fill(100), () -> {
         String msg = this.pushMessageBox.getValue().trim();
         String var10001 = msg.isEmpty() ? "" : " " + msg;
         this.sendCmd("rulestack push" + var10001);
         this.pushMessageBox.setValue("");
         this.pushMessageBox.setSuggestion(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.message_hint").getString());
      });
      if (pushTooltip != null) {
         pushButton.tooltip(pushTooltip);
      }

      this.bottomButtonLayout.child(pushButton);
      this.bottomButtonLayout.child(ScreenUtils.btn(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.btn.pop"), Sizing.fill(31), Sizing.fill(100), () -> this.sendCmd("rulestack pop")));
      this.bottomButtonLayout.child(ScreenUtils.btn(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.btn.discard"), Sizing.fill(31), Sizing.fill(100), () -> this.sendCmd("rulestack discard")));
   }

   private void togglePrefabPanel(PrefabPanel target) {
      this.prefabDynamic.clearChildren();
      if (this.prefabPanel == target) {
         this.prefabPanel = RuleStackScreen.PrefabPanel.NONE;
      } else {
         this.prefabPanel = target;
         if (target == RuleStackScreen.PrefabPanel.LIST) {
            this.fillPrefabList();
         } else {
            this.fillNewPrefabInput();
         }

      }
   }

   private void fillPrefabList() {
      RuleStackData data = CarpetGUIClient.cachedRuleStackData;
      if (data != null) {
         FlowLayout list = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
         list.surface(Surface.flat(805306368).and(Surface.outline(1090519039)));
         list.padding(Insets.of(2));

         for(String name : data.allPrefabNames()) {
            boolean active = name.equals(data.activePrefabName());
            FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
            row.padding(Insets.of(2, 2, 0, 0));
            row.verticalAlignment(VerticalAlignment.CENTER);
            row.surface(Surface.flat(active ? 1084948394 : 285212671));
            LabelComponent lbl = UIComponents.label(new net.minecraft.network.chat.TextComponent((active ? "> " : "  ") + name));
            lbl.color(Color.WHITE);
            if (!data.pendingChanges().isEmpty()) {
               row.tooltip(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.switch_warning"));
            }

            row.child(lbl);
            row.carpetGUI$mouseEnter().subscribe((MouseEnter)() -> {
               if (data.pendingChanges().isEmpty() ||
                        //? if < 26.3 {
                        InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 340)
                        //?} else {
                        /*InputConstants.isKeyDown( 340)
                        *///?}
               ) {
                  row.surface(row.surface().and(Surface.outline(Color.WHITE.argb())));
               }

            });
            row.carpetGUI$mouseLeave().subscribe((MouseLeave)() -> row.surface(Surface.flat(active ? 1084948394 : 285212671)));
            if (!active) {
               row.carpetGUI$cursorStyle(CursorStyle.HAND);
               row.carpetGUI$mouseDown().subscribe((MouseDown)(mouseButtonEvent, btn) -> {
                  if (!data.pendingChanges().isEmpty() &&
                          //? if < 26.3 {
                          !InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 340)
                          //?} else {
                          /*!InputConstants.isKeyDown( 340)
                         *///?}
                  ) {
                     return false;
                  } else {
                     this.sendCmd("rulestack prefab switch " + name);
                     return true;
                  }
               });
            }

            list.child(row);
         }

         this.prefabDynamic.child(list);
      }
   }

   private void fillNewPrefabInput() {
      FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(21));
      row.gap(3);
      TextBoxComponent nameBox = UIComponents.textBox(Sizing.fill(80));
      nameBox.setMaxLength(256);
      nameBox.setSuggestion("…");
      nameBox.carpetGUI$focusGained().subscribe((FocusGained)(s) -> nameBox.setSuggestion(""));
      row.child(nameBox);
      FlowLayout newButton = ScreenUtils.btn(new net.minecraft.network.chat.TranslatableComponent("gui.rulegroups.save"), Sizing.fill(18), Sizing.fill(98), () -> {
         String n = nameBox.getValue().trim();
         if (!n.isEmpty()) {
            this.sendCmd("rulestack prefab create " + n + " " + (/*? if < 26.3 {*/InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 342)/*?} else {*//*InputConstants.isKeyDown(342)*//*?}*/ ? "true" : "false"));
            this.prefabDynamic.clearChildren();
            this.prefabPanel = RuleStackScreen.PrefabPanel.NONE;
         }

      });
      newButton.tooltip(new net.minecraft.network.chat.TranslatableComponent("gui.tip.fork_current"));
      row.child(newButton);
      this.prefabDynamic.child(row);
   }

   private void rebuildTimeline() {
      if (this.timelineLayout != null) {
         this.timelineLayout.clearChildren();
         RuleStackData data = CarpetGUIClient.cachedRuleStackData;
         if (data == null) {
            this.timelineLayout.child(UIComponents.label(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.loading").withStyle(ChatFormatting.GRAY)));
         } else {
            List<RuleStackSyncPayload.LayerInfo> layers = data.layers();
            List<RuleStackSyncPayload.LayerInfo> futureLayers = data.futureLayers();
            boolean hasPending = !data.pendingChanges().isEmpty();
            boolean hasFuture = !futureLayers.isEmpty();
            int total = (hasPending ? 1 : 0) + futureLayers.size() + layers.size() + 1;
            int pos = 0;
            if (hasPending) {
               if (this.selectedLayerId == null) {
                  this.selectedLayerId = -1;
               }

               boolean sel = Integer.valueOf(-1).equals(this.selectedLayerId);
               this.timelineLayout.child(this.timelineNode(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.pending_changes", new Object[]{String.valueOf(data.pendingChanges().size())}).withStyle(ChatFormatting.YELLOW), (Long)null, data.pendingChanges().size(), sel, false, pos < total - 1, RuleStackScreen.NodeStyle.PENDING, this::selectPending));
               ++pos;
            }

            for(int i = 0; i < futureLayers.size(); ++i) {
               RuleStackSyncPayload.LayerInfo layer = (RuleStackSyncPayload.LayerInfo)futureLayers.get(i);
               boolean isNextRedo = i == futureLayers.size() - 1;
               int var10000 = layer.id();
               MutableComponent label = new net.minecraft.network.chat.TextComponent("↩ #" + var10000 + " ").withStyle(isNextRedo ? ChatFormatting.AQUA : ChatFormatting.DARK_AQUA);
               if (!layer.message().isEmpty()) {
                  label.append(new net.minecraft.network.chat.TextComponent("\"" + layer.message() + "\"").withStyle(ChatFormatting.UNDERLINE));
               }

               boolean sel = Integer.valueOf(layer.id()).equals(this.selectedLayerId);
               this.timelineLayout.child(this.timelineNode(label, layer.timestamp(), layer.changes().size(), sel, pos > 0, pos < total - 1, isNextRedo ? RuleStackScreen.NodeStyle.FUTURE_NEXT : RuleStackScreen.NodeStyle.FUTURE, () -> this.selectLayer(layer, true)));
               ++pos;
            }

            for(int i = layers.size() - 1; i >= 0; --i) {
               RuleStackSyncPayload.LayerInfo layer = (RuleStackSyncPayload.LayerInfo)layers.get(i);
               if (this.selectedLayerId == null && i == layers.size() - 1 && !hasPending) {
                  this.selectedLayerId = layer.id();
               }

               boolean sel = Integer.valueOf(layer.id()).equals(this.selectedLayerId);
               MutableComponent label = new net.minecraft.network.chat.TextComponent("#" + layer.id() + " ").withStyle(ChatFormatting.YELLOW);
               if (!layer.message().isEmpty()) {
                  label.append(new net.minecraft.network.chat.TextComponent("\"" + layer.message() + "\"").withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.WHITE));
               }

               this.timelineLayout.child(this.timelineNode(label, layer.timestamp(), layer.changes().size(), sel, pos > 0, pos < total - 1, i == layers.size() - 1 ? RuleStackScreen.NodeStyle.HEAD : RuleStackScreen.NodeStyle.NORMAL, () -> this.selectLayer(layer, false)));
               ++pos;
            }

            this.timelineLayout.child(this.timelineNode(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.base").withStyle(ChatFormatting.GRAY), (Long)null, 0, false, pos > 0, false, RuleStackScreen.NodeStyle.BASE, (Runnable)null));
            this.buildBottomButtons();
         }
      }
   }

   private Component rebuildPushHint() {
      RuleStackData data = CarpetGUIClient.cachedRuleStackData;
      if (data == null) {
         return null;
      } else {
         boolean hasPending = !data.pendingChanges().isEmpty();
         boolean hasFuture = !data.futureLayers().isEmpty();
         if (hasFuture && !hasPending) {
            RuleStackSyncPayload.LayerInfo next = (RuleStackSyncPayload.LayerInfo)data.futureLayers().get(data.futureLayers().size() - 1);
            int var10000 = next.id();
            String redoLabel = "#" + var10000 + (next.message().isEmpty() ? "" : " \"" + next.message() + "\"");
            return new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.push_hint.redo", new Object[]{redoLabel}).withStyle(ChatFormatting.AQUA);
         } else {
            return hasFuture ? new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.push_hint.discard_future", new Object[]{String.valueOf(data.futureLayers().size())}).withStyle(ChatFormatting.GOLD) : null;
         }
      }
   }

   private FlowLayout timelineNode(Component label, Long timestamp, int changeCount, boolean selected, boolean topLine, boolean bottomLine, NodeStyle style, Runnable onClick) {
      FlowLayout entry = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(36));
      entry.verticalAlignment(VerticalAlignment.CENTER);
      entry.padding(Insets.right(4));
      boolean isFuture = style == RuleStackScreen.NodeStyle.FUTURE || style == RuleStackScreen.NodeStyle.FUTURE_NEXT;
      if (selected) {
         int selColor = isFuture ? 631963647 : 631963562;
         int outColor = isFuture ? 1616248831 : 1621819306;
         entry.surface(Surface.flat(selColor).and(Surface.outline(outColor)));
      } else if (isFuture) {
         entry.surface(Surface.flat(402675063));
      }

      if (onClick != null) {
         entry.carpetGUI$cursorStyle(CursorStyle.HAND);
      }

      FlowLayout gutter = UIContainers.verticalFlow(Sizing.fixed(16), Sizing.fill(100));
      gutter.horizontalAlignment(HorizontalAlignment.CENTER);
      FlowLayout topConnector = UIContainers.horizontalFlow(Sizing.fixed(2), Sizing.fixed(13));
      if (topLine) {
         int lineColor = isFuture ? 1722469580 : -1716868438;
         topConnector.surface(Surface.flat(lineColor));
      }

      gutter.child(topConnector);
      FlowLayout dot = UIContainers.horizontalFlow(Sizing.fixed(8), Sizing.fixed(8));
      int dotColor;
      if (selected) {
         dotColor = isFuture ? -11141121 : -10027162;
      } else if (style == RuleStackScreen.NodeStyle.FUTURE_NEXT) {
         dotColor = -11154211;
      } else if (style == RuleStackScreen.NodeStyle.FUTURE) {
         dotColor = -13408649;
      } else if (style == RuleStackScreen.NodeStyle.BASE) {
         dotColor = -1;
      } else if (style == RuleStackScreen.NodeStyle.HEAD) {
         dotColor = -15597807;
      } else if (style == RuleStackScreen.NodeStyle.PENDING) {
         dotColor = -338365;
      } else {
         dotColor = -1431655766;
      }

      dot.surface(Surface.flat(dotColor));
      gutter.child(dot);
      FlowLayout bottomConnector = UIContainers.horizontalFlow(Sizing.fixed(2), Sizing.fill(100));
      if (bottomLine) {
         int lineColor = isFuture ? 1722469580 : -1716868438;
         bottomConnector.surface(Surface.flat(lineColor));
      }

      gutter.child(bottomConnector);
      entry.child(gutter);
      FlowLayout content = UIContainers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
      content.verticalAlignment(VerticalAlignment.CENTER);
      content.padding(Insets.left(3));
      LabelComponent lbl = UIComponents.label(label);
      if (style == RuleStackScreen.NodeStyle.FUTURE && !selected) {
         lbl.color(Color.ofArgb(-1433883990));
      } else {
         lbl.color(selected ? Color.ofArgb(-1) : Color.WHITE);
      }

      content.child(lbl);
      if (changeCount > 0 || timestamp != null) {
         String var10000 = changeCount > 0 ? changeCount + " ch" : "";
         String meta = var10000 + (timestamp != null ? (changeCount > 0 ? "  " : "") + ts(timestamp) : "");
         content.child(UIComponents.label(new net.minecraft.network.chat.TextComponent(meta).withStyle(ChatFormatting.DARK_GREEN)));
      }

      entry.child(content);
      if (onClick != null) {
         entry.carpetGUI$mouseDown().subscribe((MouseDown)(mouseButtonEvent, btn) -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onClick.run();
            return true;
         });
      }

      return entry;
   }

   private void selectLayer(RuleStackSyncPayload.LayerInfo layer, boolean isFuture) {
      this.selectedLayerId = layer.id();
      this.rebuildTimeline();
      MutableComponent header = new net.minecraft.network.chat.TextComponent("#" + layer.id() + " ").withStyle(ChatFormatting.YELLOW);
      if (!layer.message().isEmpty()) {
         header.append(new net.minecraft.network.chat.TextComponent("\"" + layer.message() + "\"").withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.WHITE));
      }

      header.append(new net.minecraft.network.chat.TextComponent(" (" + layer.changes().size() + " ch)"));
      if (isFuture) {
         header.append(new net.minecraft.network.chat.TextComponent(" ").append(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.future_marker").withStyle(ChatFormatting.AQUA)));
      }

      this.rebuildChanges(layer.changes(), header);
   }

   private void selectPending() {
      this.selectedLayerId = -1;
      this.rebuildTimeline();
      RuleStackData data = CarpetGUIClient.cachedRuleStackData;
      if (data != null) {
         this.rebuildChanges(data.pendingChanges(), new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.pending_changes", new Object[]{String.valueOf(data.pendingChanges().size())}).withStyle(ChatFormatting.AQUA));
      }
   }

   private void rebuildChanges(List<RuleStackSyncPayload.ChangeInfo> changes, Component header) {
      if (this.changesHeaderLabel != null) {
         this.changesHeaderLabel.text(header);
      }

      if (this.changesLayout != null) {
         this.changesLayout.clearChildren();
         if (changes.isEmpty()) {
            this.changesLayout.child(UIComponents.label(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.no_changes").withStyle(ChatFormatting.WHITE)));
         } else {
            for(RuleStackSyncPayload.ChangeInfo c : changes) {
               this.changesLayout.child(this.changeCard(c));
            }

         }
      }
   }

   private FlowLayout changeCard(RuleStackSyncPayload.ChangeInfo c) {
      FlowLayout card = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
      card.surface(Surface.flat(-1727855869).and(Surface.outline(301989887)));
      card.padding(Insets.of(4, 4, 7, 7));
      card.carpetGUI$margins(Insets.bottom(1));
      String managerId = c.managerId();
      if (managerId.startsWith("gamerule")) {
         managerId = managerId.split("\\$")[0];
      }

      FlowLayout nameLabel = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
      nameLabel.gap(5);
      nameLabel.verticalAlignment(VerticalAlignment.CENTER);
      String translatedName = c.ruleName();
      if (CarpetGUIClient.cachedCompleteRules.containsKey(c.ruleName())) {
         RuleData ruleData = (RuleData)CarpetGUIClient.cachedCompleteRules.get(c.ruleName());
         translatedName = ruleData.localName;
         nameLabel.tooltip(ScreenUtils.buildTooltip(ruleData, ""));
      }

      nameLabel.child(UIComponents.label(new net.minecraft.network.chat.TextComponent(translatedName)));
      nameLabel.child(UIComponents.label(new net.minecraft.network.chat.TextComponent("[" + managerId + "]").withStyle(ChatFormatting.BLUE)));
      card.child(nameLabel);
      FlowLayout valRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
      valRow.gap(5);
      valRow.verticalAlignment(VerticalAlignment.CENTER);
      valRow.padding(Insets.top(3));
      valRow.child(this.valueLabel(c.prevValue(), c.prevIsDefault(), "§c"));
      valRow.child(UIComponents.label(new net.minecraft.network.chat.TextComponent("->")));
      valRow.child(this.valueLabel(c.newValue(), c.newIsDefault(), "§a"));
      card.child(valRow);
      return card;
   }

   private LabelComponent valueLabel(String val, boolean isDefault, String color) {
      MutableComponent comp = new net.minecraft.network.chat.TextComponent(color + val);
      if (isDefault) {
         comp.append(new net.minecraft.network.chat.TranslatableComponent("commands.rulestack.change.default_marker"));
      }

      return UIComponents.label(comp);
   }

   public void onSync() {
      RuleStackData data = CarpetGUIClient.cachedRuleStackData;
      if (this.prefabNameLabel != null) {
         String name = data != null ? data.activePrefabName() : "…";
         this.prefabNameLabel.text(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.prefab", new Object[]{name}).withStyle(ChatFormatting.YELLOW));
      }

      if (this.prefabPanel == RuleStackScreen.PrefabPanel.LIST) {
         this.prefabDynamic.clearChildren();
         this.fillPrefabList();
      }

      this.selectedLayerId = null;
      this.rebuildTimeline();
      if (data != null && this.selectedLayerId != null) {
         if (this.selectedLayerId == -1) {
            if (data.pendingChanges().isEmpty()) {
               this.selectedLayerId = null;
               this.clearChangesPane();
            } else {
               this.selectPending();
            }
         } else {
            int id = this.selectedLayerId;
            Optional<RuleStackSyncPayload.LayerInfo> found = data.layers().stream().filter((l) -> l.id() == id).findFirst();
            if (found.isPresent()) {
               this.selectLayer((RuleStackSyncPayload.LayerInfo)found.get(), false);
               return;
            }

            Optional<RuleStackSyncPayload.LayerInfo> foundFuture = data.futureLayers().stream().filter((l) -> l.id() == id).findFirst();
            if (foundFuture.isPresent()) {
               this.selectLayer((RuleStackSyncPayload.LayerInfo)foundFuture.get(), true);
               return;
            }

            this.selectedLayerId = null;
            this.clearChangesPane();
         }

      }
   }

   private void clearChangesPane() {
      if (this.changesHeaderLabel != null) {
         this.changesHeaderLabel.text(new net.minecraft.network.chat.TranslatableComponent("gui.rulestack.select_layer"));
      }

      if (this.changesLayout != null) {
         this.changesLayout.clearChildren();
      }

   }

   private void requestSync() {
      CarpetGUIClientPacketHandler.send(new RequestRuleStackPayload());
   }

   private void sendCmd(String cmd) {
      ClientPacketListener conn = Minecraft.getInstance().getConnection();
      if (conn != null) {
         //? if <1.19 {
         conn.send(new net.minecraft.network.protocol.game.ServerboundChatPacket("/" + cmd));
         //?} else {
         /*conn.sendCommand(cmd);
         *///?}
         this.pendingRefreshTicks = 3;
      }

   }

   private static enum PrefabPanel {
      NONE,
      LIST,
      NEW_INPUT;

      private static PrefabPanel[] $values() {
         return new PrefabPanel[]{NONE, LIST, NEW_INPUT};
      }
   }

   private static enum NodeStyle {
      NORMAL,
      PENDING,
      HEAD,
      BASE,
      FUTURE,
      FUTURE_NEXT;

      private static NodeStyle[] $values() {
         return new NodeStyle[]{NORMAL, PENDING, HEAD, BASE, FUTURE, FUTURE_NEXT};
      }
   }
}
