package ml.mypals.carpetgui.screen.ruleGroup;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Color;
import ml.mypals.carpetgui.localChache.RulesCacheManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

import static ml.mypals.carpetgui.CarpetGUIClient.cachedManagers;
import static ml.mypals.carpetgui.CarpetGUIClient.openRuleEditScreen;
import static ml.mypals.carpetgui.screen.ScreenUtils.*;

@Environment(EnvType.CLIENT)
public class RuleGroupScreen extends BaseOwoScreen<FlowLayout> {
    private FlowLayout leftContent;
    private FlowLayout rightContent;
    private List<TextBoxComponent> currentBoxes = new ArrayList<>();
    private RuleGroup currentGroup;

    public RuleGroupScreen() {
        super();
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        cachedManagers = RulesCacheManager.loadKnownManagers();
        root.surface(Surface.blur(10,10));

        List<RuleGroup> groups = RuleGroupLoader.loadAll();

        if (groups.isEmpty()) {
            root.child(
                    Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100))
                            .horizontalAlignment(HorizontalAlignment.CENTER)
            );
            return;
        }

        FlowLayout main = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));

        this.leftContent = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        this.leftContent.gap(2);

        RuleGroup selected = groups.getFirst();
        this.currentGroup = selected;
        this.currentBoxes = new ArrayList<>();

        for (RuleCommand cmd : selected.commands()) {
            this.leftContent.child(buildRow(cmd, this.currentBoxes));
        }

        var rulesScroll = Containers.verticalScroll(
                Sizing.fill(100),
                Sizing.fill(90),
                this.leftContent
        );
        rulesScroll.surface(Surface.flat(0x19000000));

        FlowLayout bottomBar = buildBottomBar();

        FlowLayout leftPanel = Containers.verticalFlow(Sizing.fill(80), Sizing.fill(100));
        leftPanel.child(rulesScroll.sizing(Sizing.fill(100), Sizing.fill(90)));
        leftPanel.child(bottomBar.positioning(Positioning.relative(0,99))
                .sizing(Sizing.fill(100), Sizing.fill(6)));
        leftPanel.surface(Surface.flat(0x77000000));

        this.rightContent = Containers.verticalFlow(Sizing.fill(90), Sizing.content());
        rightContent.gap(2);

        for (RuleGroup group : groups) {
            rightContent.child(buildGroupEntry(group));
        }

        var rightScroll = Containers.verticalScroll(
                Sizing.fill(20),
                Sizing.fill(99),
                rightContent
        );
        rightScroll.surface(Surface.flat(0x66000000));

        main.child(leftPanel);
        main.child(rightScroll);

        root.child(main);
    }

    private FlowLayout buildBottomBar() {
        FlowLayout bar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));

        bar.surface(Surface.flat(0x30000000).and(Surface.outline(0x60000000)));
        bar.verticalAlignment(VerticalAlignment.CENTER);
        bar.gap(6);

        ButtonComponent exec = Components.button(
                Component.translatable("carpetgui.rulegroups.execute"),
                btn -> executeCurrent()
        );
        exec.sizing(Sizing.content(), Sizing.fill(100));

        ButtonComponent file = Components.button(
                Component.translatable("carpetgui.rulegroups.file"),
                btn -> Util.getPlatform().openFile(RuleGroupLoader.GROUPS_DIR.toFile())
        );
        file.sizing(Sizing.content(), Sizing.fill(100));

        ButtonComponent newGroup = Components.button(
                Component.translatable("carpetgui.rulegroups.new"),
                btn -> openRuleEditScreen(false)
        );
        newGroup.sizing(Sizing.content(), Sizing.fill(100));

        ButtonComponent addCmd = Components.button(
                Component.translatable("carpetgui.rulegroups.addcommand"),
                btn -> addCommandToCurrentGroup()
        );
        addCmd.sizing(Sizing.content(), Sizing.fill(100));
        bar.child(exec);
        bar.child(addCmd);
        bar.child(file);
        bar.child(newGroup);

        return bar;
    }
    private void addCommandToCurrentGroup() {
        if (currentGroup == null) return;
        int id = !currentGroup.commands().isEmpty() ?currentGroup.commands().getLast().id()+1 : 0;
        RuleCommand blank = new RuleCommand(
                id,
                null,
                null,
                "",
                false,
                false
        );
        currentGroup.commands().add(blank);
        leftContent.child(buildRow(blank, currentBoxes));
    }


    private void saveCurrent() {
        if (currentGroup == null) return;
        List<RuleCommand> updated = new ArrayList<>();

        for(int i = 0; i < currentGroup.commands().size(); i++){
            RuleCommand cm = currentGroup.commands().get(i);
            updated.add(new RuleCommand(
                    cm.id(),
                    cm.prefix(),
                    cm.ruleName(),
                    currentBoxes.get(i).getValue(),
                    cm.locked(),
                    cm.understandable()));
        }
        RuleGroupLoader.save(new RuleGroup(currentGroup.name(), currentGroup.filePath(), updated));
    }

    private void executeCurrent() {
        if (currentGroup == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        List<RuleCommand> cmds = currentGroup.commands();

        for (int i = 0; i < cmds.size(); i++) {
            RuleCommand cmd = cmds.get(i);
            String val = i < currentBoxes.size() ? currentBoxes.get(i).getValue() : null;

            String result = cmd.toCommand(val);
            if(result.startsWith("/")){
                mc.getConnection().sendCommand(result.substring(1));
            }else {
                mc.getConnection().sendChat(result);
            }
        }
    }

    private FlowLayout buildGroupEntry(RuleGroup group) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        boolean selected = currentGroup.name().equals(group.name());
        row.surface(Surface.flat(selected ? 0x50060606 : 0x20060606));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.horizontalAlignment(HorizontalAlignment.CENTER);
        row.cursorStyle(CursorStyle.HAND);

        String displayName = truncateWithEllipsis(group.name(),Minecraft.getInstance().font, 150);
        var nameLabel = Components.label(Component.literal(displayName))
                .color(Color.WHITE)
                .horizontalSizing(Sizing.fill(80));

        nameLabel.tooltip(Component.literal(group.name()));
        row.child(nameLabel);

        row.mouseDown().subscribe((mx, my, btn) -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK,1));
            setGroup(group);
            rebuildRightPanel();
            return true;
        });

        FlowLayout del = buildSpriteToggle(
                NO, 10, 11,
                (wrapper) -> {
                    RuleGroupLoader.delete(group);
                    rebuildRightPanel();
                    if (currentGroup != null && currentGroup.name().equals(group.name())) {
                        leftContent.clearChildren();
                        currentBoxes.clear();
                        currentGroup = null;
                    }
                }
        );

        del.sizing(Sizing.fixed(14), Sizing.fixed(14));
        row.child(del);

        return row;
    }
    private void rebuildRightPanel() {
        rightContent.clearChildren();
        for (RuleGroup group : RuleGroupLoader.loadAll()) {
            if(currentGroup == null)currentGroup = group;
            rightContent.child(buildGroupEntry(group));
        }
    }

    private FlowLayout buildRow(RuleCommand cmd, List<TextBoxComponent> valueBoxes) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(99), Sizing.fixed(10));

        row.surface(Surface.flat(0x20060606));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.horizontalAlignment(HorizontalAlignment.LEFT);


        row.mouseEnter().subscribe(() ->
                row.surface(Surface.flat(0x40060606))
        );
        row.mouseLeave().subscribe(() ->
                row.surface(Surface.flat(0x20060606))
        );

        String text = cmd.value() != null ? cmd.value() : "";
        TextBoxComponent box;
        if(cmd.prefix() != null){
            row.child(
                    Components.label(Component.literal(cmd.prefix()).withStyle(ChatFormatting.BLUE))
                            .horizontalSizing(Sizing.fill(12))
            );

            String displayName = truncateWithEllipsis(cmd.ruleName(),Minecraft.getInstance().font, 150);

            var nameLabel = Components.label(Component.literal(displayName))
                    .color(cmd.locked()?Color.ofArgb(0xFFFFD700):Color.WHITE)
                    .horizontalSizing(Sizing.fill(50));

            String defaultHint = cmd.locked()?Component.translatable("gui.tip.default").getString():"";
            nameLabel.tooltip(Component.literal(defaultHint + cmd.ruleName()));
            row.child(nameLabel);

            box = Components.textBox(Sizing.fill(30));
        }else {
            box = Components.textBox(Sizing.fill(92));
        }
        box.focusGained().subscribe((focusSource) -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK,1));
        });
        box.focusLost().subscribe(this::saveCurrent);
        box.setMaxLength(114514);
        box.text(text);
        valueBoxes.add(box);

        row.child(box);

        FlowLayout delRow = buildSpriteToggle(
                NO, 10, 11,
                (wrapper) -> {
                    currentGroup.commands().remove(cmd);
                    leftContent.removeChild(row);
                }
        );

        delRow.sizing(Sizing.fixed(12), Sizing.fixed(10)).positioning(Positioning.relative(100,0));
        row.child(delRow);

        return row;
    }

    private void setGroup(RuleGroup group) {
        this.currentGroup = group;
        this.leftContent.clearChildren();

        currentBoxes = new ArrayList<>();

        for (RuleCommand cmd : group.commands()) {
            leftContent.child(buildRow(cmd, currentBoxes));
        }
    }
}