package ml.mypals.carpetgui.screen.ruleStack;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ml.mypals.carpetgui.screen.ScreenUtils.*;

public class RuleStackScreen extends BaseOwoScreen<FlowLayout> {

    public static RuleStackScreen INSTANCE = null;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private Integer selectedLayerId = null;

    private int pendingRefreshTicks = 0;

    private enum PrefabPanel {NONE, LIST, NEW_INPUT}

    private PrefabPanel prefabPanel = PrefabPanel.NONE;

    private LabelComponent prefabNameLabel;
    private FlowLayout prefabDynamic;
    private FlowLayout timelineLayout;
    private FlowLayout changesLayout;
    private LabelComponent changesHeaderLabel;
    private TextBoxComponent pushMessageBox;

    @Override
    public void onClose() {
        INSTANCE = null;
        super.onClose();
    }

    @Override
    public void tick() {
        super.tick();
        if (pendingRefreshTicks > 0 && --pendingRefreshTicks == 0) requestSync();
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        INSTANCE = this;
        root.surface(Surface.blur(10, 10));

        root.child(buildLeftPanel());
        root.child(buildRightPanel());

        requestSync();
    }

    private FlowLayout buildLeftPanel() {
        var panel = Containers.verticalFlow(Sizing.fill(66), Sizing.content());
        panel.allowOverflow();
        changesLayout = Containers.verticalFlow(Sizing.fill(99), Sizing.content());
        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(), changesLayout);
        scroll.surface(Surface.flat(0x66000000));
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
        panel.child(scroll);

        return panel;
    }
    private FlowLayout buildRightPanel() {
        FlowLayout panel = Containers.verticalFlow(Sizing.fill(34), Sizing.fill(100));
        panel.surface(Surface.flat(0x66000000));
        panel.padding(Insets.of(2));

        FlowLayout prefabSection = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        prefabSection.margins(Insets.bottom(5));
        panel.child(prefabSection);

        prefabNameLabel = Components.label(Component.translatable("gui.rulestack.prefab", "…")
                .withStyle(ChatFormatting.YELLOW));
        prefabNameLabel.color(Color.WHITE);
        prefabNameLabel.margins(Insets.bottom(3));
        prefabSection.child(prefabNameLabel);

        FlowLayout prefabBtns = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(5));
        prefabBtns.gap(3);
        prefabBtns.child(btn(Component.translatable("gui.rulestack.btn.switch"),
                Sizing.fill(20) ,
                Sizing.fill(100) ,
                () -> togglePrefabPanel(PrefabPanel.LIST)));
        prefabBtns.child(btn(Component.translatable("gui.rulestack.btn.new_prefab"),
                Sizing.fill(20) ,
                Sizing.fill(100) ,
                () -> togglePrefabPanel(PrefabPanel.NEW_INPUT)));
        prefabSection.child(prefabBtns);

        prefabDynamic = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        prefabDynamic.margins(Insets.top(3));
        prefabSection.child(prefabDynamic);

        timelineLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        ScrollContainer<FlowLayout> timelineScroll = Containers.verticalScroll(Sizing.fill(), Sizing.fill(70), timelineLayout);
        timelineScroll.surface(Surface.flat(0x15000000));
        timelineScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
        panel.child(timelineScroll);

        String hint = Component.translatable("gui.rulestack.message_hint").getString();
        pushMessageBox = Components.textBox(Sizing.fill(100));
        pushMessageBox.setMaxLength(100);
        pushMessageBox.setSuggestion(hint);
        pushMessageBox.focusGained().subscribe(s -> pushMessageBox.setSuggestion(""));
        pushMessageBox.focusLost().subscribe(() -> {
            if (pushMessageBox.getValue().isEmpty()) pushMessageBox.setSuggestion(hint);
        });
        pushMessageBox.margins(Insets.top(4));
        panel.child(pushMessageBox);

        FlowLayout btnRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
        btnRow.gap(4);
        btnRow.margins(Insets.top(2));
        btnRow.horizontalAlignment(HorizontalAlignment.LEFT);
        btnRow.child(btn(
            Component.translatable("gui.rulestack.btn.push"),
                Sizing.fill(50) ,
                Sizing.fill(100) ,
            () -> {
            String msg = pushMessageBox.getValue().trim();
            sendCmd("rulestack push" + (msg.isEmpty() ? "" : " " + msg));
            pushMessageBox.setValue("");
            pushMessageBox.setSuggestion(hint);
        }));
        btnRow.child(btn(
                Component.translatable("gui.rulestack.btn.pop"),
                Sizing.fill(50) ,
                Sizing.fill(100) ,
                () -> sendCmd("rulestack pop")));

        panel.child(btnRow);

        return panel;
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

        var list = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        list.surface(Surface.flat(0x30000000).and(Surface.outline(0x40FFFFFF)));
        list.padding(Insets.of(2));

        for (String name : data.allPrefabNames()) {
            boolean active = name.equals(data.activePrefabName());
            FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
            row.padding(Insets.of(2, 2, 0, 0));
            row.verticalAlignment(VerticalAlignment.CENTER);
            row.surface(Surface.flat(active ? 0x40AAFFAA : 0x10FFFFFF));

            LabelComponent lbl = Components.label(Component.literal((active ? "> " : "  ") + name));
            lbl.color(Color.WHITE);
            if(!data.pendingChanges().isEmpty()) row.tooltip(Component.translatable("gui.rulestack.switch_warning"));
            row.child(lbl);
            row.mouseEnter().subscribe(() ->{
                if(data.pendingChanges().isEmpty() || Screen.hasShiftDown()) {
                    row.surface(row.surface().and(Surface.outline(Color.WHITE.argb())));
                }
            });
            row.mouseLeave().subscribe(() ->{
                row.surface(Surface.flat(active ? 0x40AAFFAA : 0x10FFFFFF));
            });
            if (!active) {
                row.cursorStyle(CursorStyle.HAND);
                row.mouseDown().subscribe((x, y, b) -> {
                    if(data.pendingChanges().isEmpty() || Screen.hasShiftDown()){
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
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(21));
        row.gap(3);

        TextBoxComponent nameBox = Components.textBox(Sizing.fill(80));
        nameBox.setMaxLength(256);
        nameBox.setSuggestion("…");
        nameBox.focusGained().subscribe(s -> nameBox.setSuggestion(""));

        row.child(nameBox);
        FlowLayout button = btn(Component.translatable("gui.rulegroups.save"),
                Sizing.fill(20) ,
                Sizing.fill(100) ,
                () -> {
            String n = nameBox.getValue().trim();
            if (!n.isEmpty()) {
                sendCmd("rulestack prefab create " + n + " false");
                prefabDynamic.clearChildren();
                prefabPanel = PrefabPanel.NONE;
            }
        });
        row.child(button);
        prefabDynamic.child(row);
    }

    private void rebuildTimeline() {
        if (timelineLayout == null) return;
        timelineLayout.clearChildren();

        RuleStackData data = CarpetGUIClient.cachedRuleStackData;
        if (data == null) {
            timelineLayout.child(Components.label(
                    Component.translatable("gui.rulestack.loading").withStyle(ChatFormatting.GRAY)));
            return;
        }

        List<RuleStackSyncPayload.LayerInfo> layers = data.layers();
        boolean hasPending = !data.pendingChanges().isEmpty();
        int total = (hasPending ? 1 : 0) + layers.size() + 1;
        int pos = 0;

        if (hasPending) {
            boolean sel = Integer.valueOf(-1).equals(selectedLayerId);
            timelineLayout.child(timelineNode(
                    Component.translatable("gui.rulestack.pending_changes",
                                    String.valueOf(data.pendingChanges().size()))
                            .withStyle(ChatFormatting.YELLOW),
                    null, data.pendingChanges().size(),
                    sel, false, pos < total - 1,
                    this::selectPending));
            pos++;
        }

        for (int i = layers.size() - 1; i >= 0; i--) {
            RuleStackSyncPayload.LayerInfo layer = layers.get(i);
            boolean sel = Integer.valueOf(layer.id()).equals(selectedLayerId);

            MutableComponent label = Component.literal("#" + layer.id() + " ").withStyle(ChatFormatting.YELLOW);
            if (!layer.message().isEmpty())
                label.append(Component.literal("\"" + layer.message() + "\"").withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.WHITE));

            final RuleStackSyncPayload.LayerInfo lRef = layer;
            timelineLayout.child(timelineNode(
                    label, layer.timestamp(), layer.changes().size(),
                    sel, pos > 0, pos < total - 1,
                    () -> selectLayer(lRef)));
            pos++;
        }

        timelineLayout.child(timelineNode(
                Component.translatable("gui.rulestack.base").withStyle(ChatFormatting.GRAY),
                null, 0,
                false, pos > 0,false,
                null));
    }

    private FlowLayout timelineNode(
            Component label, Long timestamp, int changeCount,
            boolean selected, boolean topLine, boolean bottomLine,
            Runnable onClick
    ) {
        FlowLayout entry = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(36));
        entry.verticalAlignment(VerticalAlignment.CENTER);
        entry.padding(Insets.right(4));
        if (selected) entry.surface(Surface.flat(0x25AAFFAA).and(Surface.outline(0x60AAFFAA)));
        if (onClick != null) entry.cursorStyle(CursorStyle.HAND);

        FlowLayout gutter = Containers.verticalFlow(Sizing.fixed(16), Sizing.fill(100));
        gutter.horizontalAlignment(HorizontalAlignment.CENTER);

        FlowLayout topConnector = Containers.horizontalFlow(Sizing.fixed(2), Sizing.fixed(13));
        if (topLine) topConnector.surface(Surface.flat(0x99AAAAAA));
        gutter.child(topConnector);

        FlowLayout dot = Containers.horizontalFlow(Sizing.fixed(8), Sizing.fixed(8));
        int dotColor = selected ? 0xFF66FF66
                : onClick == null ? 0xFFFFFFFF
                : 0xAAAAAAAA;
        dot.surface(Surface.flat(dotColor));
        gutter.child(dot);

        FlowLayout bottomConnector = Containers.horizontalFlow(Sizing.fixed(2), Sizing.fill(100));
        if (bottomLine) bottomConnector.surface(Surface.flat(0x99AAAAAA));
        gutter.child(bottomConnector);

        entry.child(gutter);

        FlowLayout content = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        content.verticalAlignment(VerticalAlignment.CENTER);
        content.padding(Insets.left(3));

        LabelComponent lbl = Components.label(label);
        lbl.color(selected ? Color.ofArgb(0xFFFFFFFF) : Color.WHITE);
        content.child(lbl);

        if (changeCount > 0 || timestamp != null) {
            String meta = (changeCount > 0 ? changeCount + " ch" : "")
                    + (timestamp != null ? (changeCount > 0 ? "  " : "") + ts(timestamp) : "");
            content.child(Components.label(Component.literal(meta).withStyle(ChatFormatting.DARK_GREEN)));
        }

        entry.child(content);

        if (onClick != null)
            entry.mouseDown().subscribe((x, y, b) -> {
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
                onClick.run();
                return true;
            });

        return entry;
    }

    private void selectLayer(RuleStackSyncPayload.LayerInfo layer) {
        selectedLayerId = layer.id();
        rebuildTimeline();

        MutableComponent header = Component.literal("#" + layer.id() + " ").withStyle(ChatFormatting.YELLOW);
        if (!layer.message().isEmpty())
            header.append(Component.literal("\"" + layer.message() + "\"").withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.WHITE));
        header.append(Component.literal(" (" + layer.changes().size() + " ch)"));

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

    private void rebuildChanges(List<RuleStackSyncPayload.ChangeInfo> changes, Component header) {
        if (changesHeaderLabel != null) changesHeaderLabel.text(header);
        if (changesLayout == null) return;
        changesLayout.clearChildren();
        if (changes.isEmpty()) {
            changesLayout.child(Components.label(
                    Component.translatable("gui.rulestack.no_changes")
                            .withStyle(ChatFormatting.WHITE)));
            return;
        }
        for (var c : changes) changesLayout.child(changeCard(c));
    }

    private FlowLayout changeCard(RuleStackSyncPayload.ChangeInfo c) {
        var card = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        card.surface(Surface.flat(0x99030303).and(Surface.outline(0x11FFFFFF)));
        card.padding(Insets.of(4, 4, 7, 7));
        card.margins(Insets.bottom(1));

        String managerId = c.managerId();
        if(managerId.startsWith("gamerule")){managerId = managerId.split("\\$")[0];}

        var nameRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        nameRow.gap(5);
        nameRow.verticalAlignment(VerticalAlignment.CENTER);
        nameRow.child(Components.label(Component.literal( c.ruleName())));
        nameRow.child(Components.label(Component.literal("[" + managerId + "]").withStyle(ChatFormatting.BLUE)));
        card.child(nameRow);

        var valRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        valRow.gap(5);
        valRow.verticalAlignment(VerticalAlignment.CENTER);
        valRow.padding(Insets.top(3));
        valRow.child(valueLabel(c.prevValue(), c.prevIsDefault(), "§c"));
        valRow.child(Components.label(Component.literal("->")));
        valRow.child(valueLabel(c.newValue(), c.newIsDefault(), "§a"));
        card.child(valRow);

        return card;
    }

    private LabelComponent valueLabel(String val, boolean isDefault, String color) {
        MutableComponent comp = Component.literal(color + val);
        if (isDefault)
            comp.append(Component.translatable("commands.rulestack.change.default_marker"));
        return Components.label(comp);
    }

    public void onSync() {
        var data = CarpetGUIClient.cachedRuleStackData;
         if (prefabNameLabel != null) {
            String name = data != null ? data.activePrefabName() : "…";
            prefabNameLabel.text(Component.translatable("gui.rulestack.prefab", name)
                    .withStyle(ChatFormatting.YELLOW));
        }
        if (prefabPanel == PrefabPanel.LIST) {
            prefabDynamic.clearChildren();
            fillPrefabList();
        }

        rebuildTimeline();

        if (data == null || selectedLayerId == null) return;

        if (selectedLayerId == -1) {
            if (data.pendingChanges().isEmpty()) {
                selectedLayerId = null;
                clearChangesPane();
            } else selectPending();
        } else {
            int id = selectedLayerId;
            data.layers().stream().filter(l -> l.id() == id).findFirst()
                    .ifPresentOrElse(
                            this::selectLayer,
                            () -> {
                                selectedLayerId = null;
                                clearChangesPane();
                            });
        }
    }

    private void clearChangesPane() {
        if (changesHeaderLabel != null)
            changesHeaderLabel.text(Component.translatable("gui.rulestack.select_layer"));
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

    private static String ts(long ms) {
        return FMT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault()));
    }
}