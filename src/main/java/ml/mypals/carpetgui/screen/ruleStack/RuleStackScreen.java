package ml.mypals.carpetgui.screen.ruleStack;

import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;
import ml.mypals.carpetgui.screen.ScreenSwitcherScreen;
import ml.mypals.carpetgui.screen.ScreenTabBar;
import ml.mypals.carpetgui.screen.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ml.mypals.carpetgui.screen.ScreenUtils.btn;
import static ml.mypals.carpetgui.screen.ScreenUtils.makeMasterContainer;

public class RuleStackScreen extends BaseOwoScreen<FlowLayout> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    public static RuleStackScreen INSTANCE = null;
    private Integer selectedLayerId = null;

    private int pendingRefreshTicks = 0;
    private PrefabPanel prefabPanel = PrefabPanel.NONE;
    private LabelComponent prefabNameLabel;
    private FlowLayout prefabDynamic;
    private FlowLayout timelineLayout;
    private FlowLayout changesLayout;
    private FlowLayout bottomButtonLayout;
    private LabelComponent changesHeaderLabel;
    private TextBoxComponent pushMessageBox;

    private static String ts(long ms) {
        return FMT.format(
                LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault()));
    }

    @Override
    public void tick() {
        super.tick();
        if (pendingRefreshTicks > 0 && --pendingRefreshTicks == 0) requestSync();
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/::verticalFlow);
    }
    @Override
    protected void build(FlowLayout root) {
        ScreenTabBar.build(buildMain(root), ScreenTabBar.Tab.STACK);
    }
    protected FlowLayout buildMain(FlowLayout root) {
        INSTANCE = this;
        var master = makeMasterContainer(this.width, this.height, root);

        master.getValue().child(buildLeftPanel());
        master.getValue().child(buildRightPanel());
        root.child(master.getKey());
        requestSync();
        return master.getKey();
    }

    private FlowLayout buildLeftPanel() {
        var panel = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(66), Sizing.content());
        panel.allowOverflow();
        changesLayout = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(99), Sizing.content());
        ScrollContainer<FlowLayout> scroll =
                /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalScroll(Sizing.fill(100), Sizing.fill(100), changesLayout);
        scroll.surface(Surface.outline(0x66000000));
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
        scroll.padding(Insets.of(2,2,2,2));
        panel.child(scroll);
        return panel;
    }

    private FlowLayout buildRightPanel() {
        FlowLayout panel = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(34), Sizing.fill(100));
        panel.surface(Surface.outline(0x66000000));
        panel.padding(Insets.of(2));

        FlowLayout prefabSection =
                /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(100), Sizing.content());
        prefabSection.margins(Insets.bottom(5));
        panel.child(prefabSection);

        prefabNameLabel = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(
                Component.translatable("gui.rulestack.prefab", "…")
                        .withStyle(ChatFormatting.YELLOW));
        prefabNameLabel.color(Color.WHITE);
        prefabNameLabel.margins(Insets.bottom(3));
        prefabSection.child(prefabNameLabel);

        FlowLayout prefabBtns =
                /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.fill(5));
        prefabBtns.gap(3);
        prefabBtns.child(btn(Component.translatable("gui.rulestack.btn.switch"),
                Sizing.fill(20), Sizing.fill(100),
                () -> togglePrefabPanel(PrefabPanel.LIST)));
        prefabBtns.child(btn(Component.translatable("gui.rulestack.btn.new_prefab"),
                Sizing.fill(20), Sizing.fill(100),
                () -> togglePrefabPanel(PrefabPanel.NEW_INPUT)));
        prefabSection.child(prefabBtns);

        prefabDynamic = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(100), Sizing.content());
        prefabDynamic.margins(Insets.top(3));
        prefabSection.child(prefabDynamic);

        timelineLayout = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(100), Sizing.content());
        ScrollContainer<FlowLayout> timelineScroll =
                /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalScroll(Sizing.fill(100), Sizing.fill(60), timelineLayout);
        timelineScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
        panel.child(timelineScroll);

        String hint = Component.translatable("gui.rulestack.message_hint").getString();
        pushMessageBox = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.textBox(Sizing.fill(100));
        pushMessageBox.setMaxLength(100);
        pushMessageBox.setSuggestion(hint);
        pushMessageBox.focusGained().subscribe(s -> pushMessageBox.setSuggestion(""));
        pushMessageBox.focusLost().subscribe(() -> {
            if (pushMessageBox.getValue().isEmpty())
                pushMessageBox.setSuggestion(hint);
        });
        pushMessageBox.margins(Insets.top(4));
        panel.child(pushMessageBox);

        bottomButtonLayout = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
        bottomButtonLayout.gap(4);
        bottomButtonLayout.margins(Insets.top(2));
        bottomButtonLayout.horizontalAlignment(HorizontalAlignment.LEFT);

        buildBottomButtons();

        panel.child(bottomButtonLayout);
        return panel;
    }
    private void buildBottomButtons() {
        bottomButtonLayout.clearChildren();
        Component pushTooltip = rebuildPushHint();
        FlowLayout pushButton = btn(
                Component.translatable("gui.rulestack.btn.push"),
                Sizing.fill(50), Sizing.fill(100),
                () -> {
                    String msg = pushMessageBox.getValue().trim();
                    sendCmd("rulestack push" + (msg.isEmpty() ? "" : " " + msg));
                    pushMessageBox.setValue("");
                    pushMessageBox.setSuggestion(Component.translatable("gui.rulestack.message_hint").getString());
                });
        if (pushTooltip != null) {
            pushButton.tooltip(pushTooltip);
        }
        bottomButtonLayout.child(pushButton);

        bottomButtonLayout.child(btn(
                Component.translatable("gui.rulestack.btn.pop"),
                Sizing.fill(50), Sizing.fill(100),
                () -> sendCmd("rulestack pop")));
    }

    private void togglePrefabPanel(PrefabPanel target) {
        prefabDynamic.clearChildren();
        if (prefabPanel == target) {
            prefabPanel = PrefabPanel.NONE;
            return;
        }
        prefabPanel = target;
        if (target == PrefabPanel.LIST) fillPrefabList();
        else fillNewPrefabInput();
    }

    private void fillPrefabList() {
        var data = CarpetGUIClient.cachedRuleStackData;
        if (data == null) return;

        var list = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(100), Sizing.content());
        list.surface(Surface.flat(0x30000000).and(Surface.outline(0x40FFFFFF)));
        list.padding(Insets.of(2));

        for (String name : data.allPrefabNames()) {
            boolean active = name.equals(data.activePrefabName());
            FlowLayout row = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
            row.padding(Insets.of(2, 2, 0, 0));
            row.verticalAlignment(VerticalAlignment.CENTER);
            row.surface(Surface.flat(active ? 0x40AAFFAA : 0x10FFFFFF));

            LabelComponent lbl = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(
                    Component.literal((active ? "> " : "  ") + name));
            lbl.color(Color.WHITE);
            if (!data.pendingChanges().isEmpty())
                row.tooltip(Component.translatable("gui.rulestack.switch_warning"));
            row.child(lbl);

            row.mouseEnter().subscribe(() -> {
                if (data.pendingChanges().isEmpty()
                        || InputConstants.isKeyDown(minecraft.getWindow()/*? if <1.21.9 {*//*.getWindow()*//*?}*/, GLFW.GLFW_KEY_LEFT_SHIFT))
                    row.surface(row.surface().and(Surface.outline(Color.WHITE.argb())));
            });
            row.mouseLeave().subscribe(() ->
                    row.surface(Surface.flat(active ? 0x40AAFFAA : 0x10FFFFFF)));

            if (!active) {
                row.cursorStyle(CursorStyle.HAND);
                //? if <1.21.9 {
                /*row.mouseDown().subscribe((x, y, btn) -> {
                *///?} else {
                row.mouseDown().subscribe((mouseButtonEvent, btn) -> {
                //?}
                    if (data.pendingChanges().isEmpty()
                            ||  InputConstants.isKeyDown(minecraft.getWindow()/*? if <1.21.9 {*//*.getWindow()*//*?}*/, GLFW.GLFW_KEY_LEFT_SHIFT)) {
                        sendCmd("rulestack prefab switch " + name);
                        return true;
                    }
                    return false;
                });
            }
            list.child(row);
        }
        prefabDynamic.child(list);
    }

    private void fillNewPrefabInput() {
        FlowLayout row = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.fixed(21));
        row.gap(3);
        TextBoxComponent nameBox = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.textBox(Sizing.fill(80));
        nameBox.setMaxLength(256);
        nameBox.setSuggestion("…");
        nameBox.focusGained().subscribe(s -> nameBox.setSuggestion(""));
        row.child(nameBox);
        FlowLayout newButton = btn(Component.translatable("gui.rulegroups.save"),
                Sizing.fill(20), Sizing.fill(100),
                () -> {
                    String n = nameBox.getValue().trim();
                    if (!n.isEmpty()) {
                        sendCmd("rulestack prefab create " + n + " " + ( InputConstants.isKeyDown(minecraft.getWindow()/*? if <1.21.9 {*//*.getWindow()*//*?}*/, GLFW.GLFW_KEY_LEFT_ALT)?"true":"false"));
                        prefabDynamic.clearChildren();
                        prefabPanel = PrefabPanel.NONE;
                    }
                });
        newButton.tooltip(Component.translatable("gui.tip.fork_current"));
        row.child(newButton);
        prefabDynamic.child(row);
    }

    private void rebuildTimeline() {
        if (timelineLayout == null) return;
        timelineLayout.clearChildren();

        RuleStackData data = CarpetGUIClient.cachedRuleStackData;
        if (data == null) {
            timelineLayout.child(/*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(
                    Component.translatable("gui.rulestack.loading")
                            .withStyle(ChatFormatting.GRAY)));
            return;
        }

        List<RuleStackSyncPayload.LayerInfo> layers = data.layers();
        List<RuleStackSyncPayload.LayerInfo> futureLayers = data.futureLayers();
        boolean hasPending = !data.pendingChanges().isEmpty();
        boolean hasFuture = !futureLayers.isEmpty();

        int total = (hasPending ? 1 : 0) + futureLayers.size() + layers.size() + 1;
        int pos = 0;

        if (hasPending) {
            if(selectedLayerId == null) selectedLayerId = -1;
            boolean sel = Integer.valueOf(-1).equals(selectedLayerId);
            timelineLayout.child(timelineNode(
                    Component.translatable("gui.rulestack.pending_changes",
                                    String.valueOf(data.pendingChanges().size()))
                            .withStyle(ChatFormatting.YELLOW),
                    null, data.pendingChanges().size(),
                    sel, false, pos < total - 1,
                    NodeStyle.PENDING,
                    this::selectPending));
            pos++;
        }

        for (int i = 0; i < futureLayers.size(); i++) {
            RuleStackSyncPayload.LayerInfo layer = futureLayers.get(i);
            boolean isNextRedo = (i == futureLayers.size() - 1);

            MutableComponent label =
                    Component.literal("↩ #" + layer.id() + " ")
                            .withStyle(isNextRedo ? ChatFormatting.AQUA
                                    : ChatFormatting.DARK_AQUA);
            if (!layer.message().isEmpty())
                label.append(Component.literal("\"" + layer.message() + "\"")
                        .withStyle(ChatFormatting.UNDERLINE));

            boolean sel = Integer.valueOf(layer.id()).equals(selectedLayerId);
            final RuleStackSyncPayload.LayerInfo lRef = layer;
            timelineLayout.child(timelineNode(
                    label, layer.timestamp(), layer.changes().size(),
                    sel, pos > 0, pos < total - 1,
                    isNextRedo ? NodeStyle.FUTURE_NEXT : NodeStyle.FUTURE,
                    () -> selectLayer(lRef, true)));
            pos++;
        }

        for (int i = layers.size() - 1; i >= 0; i--) {
            RuleStackSyncPayload.LayerInfo layer = layers.get(i);
            if(selectedLayerId == null && i == layers.size()-1 && ! hasPending){
                selectedLayerId = layer.id();
            }

            boolean sel = Integer.valueOf(layer.id()).equals(selectedLayerId);

            MutableComponent label =
                    Component.literal("#" + layer.id() + " ").withStyle(ChatFormatting.YELLOW);
            if (!layer.message().isEmpty())
                label.append(Component.literal("\"" + layer.message() + "\"")
                        .withStyle(ChatFormatting.UNDERLINE)
                        .withStyle(ChatFormatting.WHITE));

            final RuleStackSyncPayload.LayerInfo lRef = layer;
            timelineLayout.child(timelineNode(
                    label, layer.timestamp(), layer.changes().size(),
                    sel, pos > 0, pos < total - 1,
                    i == layers.size() - 1 ? NodeStyle.HEAD : NodeStyle.NORMAL,
                    () -> selectLayer(lRef, false)));
            pos++;
        }

        timelineLayout.child(timelineNode(
                Component.translatable("gui.rulestack.base").withStyle(ChatFormatting.GRAY),
                null, 0,
                false, pos > 0, false,
                NodeStyle.BASE,
                null));
        buildBottomButtons();
    }

    private Component rebuildPushHint() {
        RuleStackData data = CarpetGUIClient.cachedRuleStackData;
        if (data == null) {
            return null;
        }

        boolean hasPending = !data.pendingChanges().isEmpty();
        boolean hasFuture = !data.futureLayers().isEmpty();

        if (hasFuture && !hasPending) {
            RuleStackSyncPayload.LayerInfo next =
                    data.futureLayers().getLast();
            String redoLabel = "#" + next.id()
                    + (next.message().isEmpty() ? "" : " \"" + next.message() + "\"");
            return Component.translatable("gui.rulestack.push_hint.redo", redoLabel)
                    .withStyle(ChatFormatting.AQUA);
        } else if (hasFuture) {

            return Component.translatable("gui.rulestack.push_hint.discard_future",
                            String.valueOf(data.futureLayers().size()))
                    .withStyle(ChatFormatting.GOLD);
        } else {
            return null;
        }
    }

    private FlowLayout timelineNode(
            Component label,
            Long timestamp,
            int changeCount,
            boolean selected,
            boolean topLine,
            boolean bottomLine,
            NodeStyle style,
            Runnable onClick
    ) {
        FlowLayout entry = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.fixed(36));
        entry.verticalAlignment(VerticalAlignment.CENTER);
        entry.padding(Insets.right(4));

        boolean isFuture = (style == NodeStyle.FUTURE || style == NodeStyle.FUTURE_NEXT);

        if (selected) {
            int selColor = isFuture ? 0x25AAFFFF : 0x25AAFFAA;
            int outColor = isFuture ? 0x6055FFFF : 0x60AAFFAA;
            entry.surface(Surface.flat(selColor).and(Surface.outline(outColor)));
        } else if (isFuture) {
            entry.surface(Surface.flat(0x18005577));
        }
        if (onClick != null) entry.cursorStyle(CursorStyle.HAND);

        FlowLayout gutter = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fixed(16), Sizing.fill(100));
        gutter.horizontalAlignment(HorizontalAlignment.CENTER);

        FlowLayout topConnector = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fixed(2), Sizing.fixed(13));
        if (topLine) {
            int lineColor = isFuture ? 0x66AACCCC : 0x99AAAAAA;
            topConnector.surface(Surface.flat(lineColor));
        }
        gutter.child(topConnector);

        FlowLayout dot = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fixed(8), Sizing.fixed(8));
        int dotColor;
        if (selected) {
            dotColor = isFuture ? 0xFF55FFFF : 0xFF66FF66;
        } else if (style == NodeStyle.FUTURE_NEXT) {
            dotColor = 0xFF55CCDD;
        } else if (style == NodeStyle.FUTURE) {
            dotColor = 0xFF336677;
        } else if (style == NodeStyle.BASE) {
            dotColor = 0xFFFFFFFF;
        } else if (style == NodeStyle.HEAD) {
            dotColor = 0xFF11FF11;
        } else if (style == NodeStyle.PENDING) {
            dotColor = 0xFFFAD643;
        } else {
            dotColor = 0xAAAAAAAA;
        }
        dot.surface(Surface.flat(dotColor));
        gutter.child(dot);

        FlowLayout bottomConnector =
                /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fixed(2), Sizing.fill(100));
        if (bottomLine) {
            int lineColor = isFuture ? 0x66AACCCC : 0x99AAAAAA;
            bottomConnector.surface(Surface.flat(lineColor));
        }
        gutter.child(bottomConnector);

        entry.child(gutter);

        FlowLayout content = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        content.verticalAlignment(VerticalAlignment.CENTER);
        content.padding(Insets.left(3));

        LabelComponent lbl = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(label);
        if (style == NodeStyle.FUTURE && !selected) {
            lbl.color(Color.ofArgb(0xAA88AAAA));
        } else {
            lbl.color(selected ? Color.ofArgb(0xFFFFFFFF) : Color.WHITE);
        }
        content.child(lbl);

        if (changeCount > 0 || timestamp != null) {
            String meta = (changeCount > 0 ? changeCount + " ch" : "")
                    + (timestamp != null
                    ? (changeCount > 0 ? "  " : "") + ts(timestamp)
                    : "");
            content.child(/*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(
                    Component.literal(meta).withStyle(ChatFormatting.DARK_GREEN))
            );
        }

        entry.child(content);

        if (onClick != null) {

            //? if <1.21.9 {
            /*entry.mouseDown().subscribe((x, y, btn) -> {
            *///?} else {
            entry.mouseDown().subscribe((mouseButtonEvent, btn) -> {
            //?}

                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
                onClick.run();
                return true;
            });
        }

        return entry;
    }

    private void selectLayer(RuleStackSyncPayload.LayerInfo layer, boolean isFuture) {
        selectedLayerId = layer.id();
        rebuildTimeline();

        MutableComponent header =
                Component.literal("#" + layer.id() + " ").withStyle(ChatFormatting.YELLOW);
        if (!layer.message().isEmpty())
            header.append(Component.literal("\"" + layer.message() + "\"")
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(ChatFormatting.WHITE));
        header.append(Component.literal(" (" + layer.changes().size() + " ch)"));
        if (isFuture)
            header.append(Component.literal(" ")
                    .append(Component.translatable("gui.rulestack.future_marker")
                            .withStyle(ChatFormatting.AQUA)));

        rebuildChanges(layer.changes(), header);
    }

    private void selectPending() {
        selectedLayerId = -1;
        rebuildTimeline();
        var data = CarpetGUIClient.cachedRuleStackData;
        if (data == null) return;
        rebuildChanges(data.pendingChanges(),
                Component.translatable("gui.rulestack.pending_changes",
                                String.valueOf(data.pendingChanges().size()))
                        .withStyle(ChatFormatting.AQUA));
    }

    private void rebuildChanges(List<RuleStackSyncPayload.ChangeInfo> changes,
                                Component header) {
        if (changesHeaderLabel != null) changesHeaderLabel.text(header);
        if (changesLayout == null) return;
        changesLayout.clearChildren();
        if (changes.isEmpty()) {
            changesLayout.child(/*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(
                    Component.translatable("gui.rulestack.no_changes")
                            .withStyle(ChatFormatting.WHITE)));
            return;
        }
        for (var c : changes) changesLayout.child(changeCard(c));
    }

    private FlowLayout changeCard(RuleStackSyncPayload.ChangeInfo c) {
        var card = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(100), Sizing.content());
        card.surface(Surface.flat(0x99030303).and(Surface.outline(0x11FFFFFF)));
        card.padding(Insets.of(4, 4, 7, 7));
        card.margins(Insets.bottom(1));

        String managerId = c.managerId();
        if (managerId.startsWith("gamerule")) {
            managerId = managerId.split("\\$")[0];
        }

        var nameRow = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.content());
        nameRow.gap(5);
        nameRow.verticalAlignment(VerticalAlignment.CENTER);
        nameRow.child(/*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(Component.literal(c.ruleName())));
        nameRow.child(/*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(
                Component.literal("[" + managerId + "]").withStyle(ChatFormatting.BLUE)));
        card.child(nameRow);

        var valRow = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.content());
        valRow.gap(5);
        valRow.verticalAlignment(VerticalAlignment.CENTER);
        valRow.padding(Insets.top(3));
        valRow.child(valueLabel(c.prevValue(), c.prevIsDefault(), "§c"));
        valRow.child(/*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(Component.literal("->")));
        valRow.child(valueLabel(c.newValue(), c.newIsDefault(), "§a"));
        card.child(valRow);

        return card;
    }

    private LabelComponent valueLabel(String val, boolean isDefault, String color) {
        MutableComponent comp = Component.literal(color + val);
        if (isDefault)
            comp.append(Component.translatable("commands.rulestack.change.default_marker"));
        return /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(comp);
    }

    public void onSync() {
        var data = CarpetGUIClient.cachedRuleStackData;
        if (prefabNameLabel != null) {
            String name = data != null ? data.activePrefabName() : "…";
            prefabNameLabel.text(
                    Component.translatable("gui.rulestack.prefab", name)
                            .withStyle(ChatFormatting.YELLOW));
        }
        if (prefabPanel == PrefabPanel.LIST) {
            prefabDynamic.clearChildren();
            fillPrefabList();
        }

        selectedLayerId = null;
        rebuildTimeline();

        if (data == null || selectedLayerId == null) return;

        if (selectedLayerId == -1) {
            if (data.pendingChanges().isEmpty()) {
                selectedLayerId = null;
                clearChangesPane();
            } else {
                selectPending();
            }
        } else {
            int id = selectedLayerId;
            var found = data.layers().stream().filter(l -> l.id() == id).findFirst();
            if (found.isPresent()) {
                selectLayer(found.get(), false);
                return;
            }
            var foundFuture = data.futureLayers().stream()
                    .filter(l -> l.id() == id).findFirst();
            if (foundFuture.isPresent()) {
                selectLayer(foundFuture.get(), true);
                return;
            }
            selectedLayerId = null;
            clearChangesPane();
        }
    }

    private void clearChangesPane() {
        if (changesHeaderLabel != null)
            changesHeaderLabel.text(
                    Component.translatable("gui.rulestack.select_layer"));
        if (changesLayout != null) changesLayout.clearChildren();
    }

    private void requestSync() {
        ClientPlayNetworking.send(new RequestRuleStackPayload());
    }

    private void sendCmd(String cmd) {
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            conn.sendCommand(cmd);
            pendingRefreshTicks = 3;
        }
    }



    private enum PrefabPanel {NONE, LIST, NEW_INPUT}

    private enum NodeStyle {
        NORMAL,
        PENDING,
        HEAD,
        BASE,
        FUTURE,
        FUTURE_NEXT
    }
}