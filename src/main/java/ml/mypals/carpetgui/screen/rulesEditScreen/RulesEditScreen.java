package ml.mypals.carpetgui.screen.rulesEditScreen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import ml.mypals.carpetgui.mixin.accessors.ScrollContentAccessor;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.screen.ScreenTabBar;
import ml.mypals.carpetgui.screen.ScreenUtils;
import ml.mypals.carpetgui.screen.ruleGroup.RuleCommand;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroup;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupLoader;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static ml.mypals.carpetgui.CarpetGUI.MOD_ID;
import static ml.mypals.carpetgui.CarpetGUIClient.*;
import static ml.mypals.carpetgui.screen.ScreenUtils.makeMasterContainer;

public class RulesEditScreen extends BaseOwoScreen<FlowLayout> {
    private static final Logger log = LoggerFactory.getLogger(RulesEditScreen.class);
    public boolean instantAffect;
    private FlowLayout saveDialog;
    private OverlayContainer<FlowLayout> dialogOverlay;
    public double lastCategoryScroll = 0;
    public double lastRuleListScroll = 0;
    public String currentCategory = "unknown";
    public String lastCategoryBeforeSearching = currentCategory;
    public boolean searching = false;

    public enum DefaultCategory {
        ALL("all"),
        SEARCHING("searching"),
        DEFAULT("default"),
        GAMERULES("gamerules"),
        FAVORITE("favorite"),
        MODIFIED("modified");
        private final String name;

        DefaultCategory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Component getDisplayName(String name) {
            for (DefaultCategory df : DefaultCategory.values()) {
                if (df.getName().equals(name)) {
                    return Component.translatable("gui.category." + name);
                }
            }
            return Component.translatable(name);
        }
    }

    private FlowLayout rulesListLayout;
    private FlowLayout categoriesListLayout;
    private LabelComponent currentCategoryLabel;
    private TextBoxComponent searchBox;
    private ScrollContainer<FlowLayout> categoriesScroll;
    private ScrollContainer<FlowLayout> rulesScroll;


    public RulesEditScreen(boolean instantAffect) {
        super();
        this.instantAffect = instantAffect;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/::verticalFlow);
    }
    @Override
    protected void build(FlowLayout root) {
        ScreenTabBar.build(buildMain(root), ScreenTabBar.Tab.RULES);
    }
    protected FlowLayout buildMain(FlowLayout root) {

        var master = makeMasterContainer(this.width, this.height, root);

        ScreenUtils.DialogResult dialogResult = ScreenUtils.createSaveGroupDialog(
                this::saveModifiedRulesAsGroup,
                (ingnored) -> {
                    ScreenUtils.hideSaveDialog(this.uiAdapter.rootComponent, dialogOverlay);
                    if (!this.instantAffect) {
                        instantAffect = true;
                        Minecraft.getInstance().setScreen(new RuleGroupScreen());
                    }
                });

        saveDialog = dialogResult.dialog();
        dialogOverlay = dialogResult.overlay();

        var leftPanel = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(66), Sizing.fill(100));
        leftPanel.padding(Insets.of(5));


        var searchRow = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.fill(10));
        searchRow.verticalAlignment(VerticalAlignment.CENTER);
        searchRow.padding(Insets.of(2, 2, 4, 4));
        searchRow.surface(Surface.flat(0x0AAAAAAA));

        var searchIcon = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.texture(
                Identifier.fromNamespaceAndPath(MOD_ID, "ui/search.png"),
                0, 0, 10, 11, 10, 11);
        searchIcon.sizing(Sizing.fixed(10), Sizing.fixed(11));
        searchRow.child(searchIcon);

        searchBox = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.textBox(Sizing.fill(100));
        searchBox.setMaxLength(100);
        searchBox.setFocused(true);
        searchBox.onChanged().subscribe(this::onSearch);
        searchBox.focusGained().subscribe(source -> this.onSearch(searchBox.getValue()));
        searchRow.child(searchBox);
        leftPanel.child(searchRow);
        rulesListLayout = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(99), Sizing.content());
        rulesScroll = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalScroll(
                Sizing.fill(100), Sizing.fill(90), rulesListLayout);
        rulesScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
        leftPanel.child(rulesScroll);


        var rightPanel = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(34), Sizing.fill(100));

        rightPanel.padding(Insets.of(5));

        currentCategoryLabel = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(Component.nullToEmpty(currentCategory));
        currentCategoryLabel.color(Color.WHITE);
        currentCategoryLabel.margins(Insets.of(2, 6, 4, 0));
        rightPanel.child(currentCategoryLabel);

        categoriesListLayout = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(98), Sizing.content());
        categoriesScroll = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalScroll(
                Sizing.fill(100), Sizing.fill(100), categoriesListLayout);
        categoriesScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
        categoriesScroll.surface(Surface.flat(0x19000000));
        categoriesScroll.scrollbarThiccness(10);
        rightPanel.child(categoriesScroll);

        master.getValue().child(leftPanel);
        master.getValue().child(rightPanel);

        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.verticalAlignment(VerticalAlignment.CENTER);

        root.child(master.getKey());

        setCurrentCategory(DefaultCategory.ALL.getName());



        //? if <1.21.9 {
        /*ScreenKeyboardEvents.afterKeyPress(this).register((screen, key, scancode, modifiers) -> {
            if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && key == GLFW.GLFW_KEY_S) {
                ScreenUtils.showSaveGroupDialog(this.uiAdapter.rootComponent, dialogOverlay);
            }
        });
        *///?} else {
        ScreenKeyboardEvents.afterKeyPress(this).register((screen, key) -> {
            if ((key.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && key.key() == GLFW.GLFW_KEY_S) {
                ScreenUtils.showSaveGroupDialog(this.uiAdapter.rootComponent, dialogOverlay);
            }
        });
        //?}
        return master.getKey();
    }

    private void saveModifiedRulesAsGroup(String groupName) {
        List<RuleData> modifiedRules = cachedCompleteRules.values().stream()
                .filter(r -> !Objects.equals(r.defaultValue, r.value) || defaultRules.contains(r.name))
                .toList();

        if (modifiedRules.isEmpty()) return;

        List<RuleCommand> commands = modifiedRules.stream()
                .map(rule -> {
                    String prefix = rule.isGamerule ? "gamerule" : rule.manager;
                    return new RuleCommand(-1, prefix, rule.name, rule.value, defaultRules.contains(rule.name), true);
                })
                .toList();

        Path path = RuleGroupLoader.GROUPS_DIR.resolve(groupName + ".txt");

        RuleGroup group = new RuleGroup(groupName, path, commands);
        boolean success = RuleGroupLoader.save(group);

        if (success) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.RESPAWN_ANCHOR_CHARGE, 5));
        }
    }

    public void onSearch(String input) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        if (!Objects.equals(currentCategory, DefaultCategory.SEARCHING.getName()))
            lastCategoryBeforeSearching = currentCategory;
        if (input.isEmpty()) {
            searching = false;
            setCurrentCategory(lastCategoryBeforeSearching);
        } else {
            searching = true;


            double roffset = ((ScrollContentAccessor) this.rulesScroll).carpetGUI$getScrollOffset();
            var rmax = ((ScrollContentAccessor) this.rulesScroll).carpetGUI$getMaxScroll();

            setCurrentCategory(DefaultCategory.SEARCHING.getName());
            rebuildRulesList(
                    cachedCompleteRules.values().stream().filter(r -> {
                        List<String> parts = new ArrayList<>();
                        parts.add(r.name);
                        parts.add(r.localName);
                        parts.add(r.description);
                        parts.add(r.localDescription);
                        parts.addAll(r.categories.stream().map(Map.Entry::getKey).toList());
                        parts.addAll(r.categories.stream().map(Map.Entry::getValue).toList());
                        return matchesRule(parts, input);
                    })
                    , searchBox.getValue()
            );

        //? if >1.19.4 {
        this.rulesScroll.scrollTo(lastRuleListScroll);
        //?} else {
        /*((ScrollContentAccessor)this.rulesScroll).carpetGUI$setScrollOffset(lastRuleListScroll);
        *///?}

        }
    }

    public void refreshScreen() {
        if (Objects.equals(currentCategory, DefaultCategory.SEARCHING.getName())) {
            onSearch(searchBox.getValue());
        } else {
            setCurrentCategory(this.currentCategory);
        }
    }

    public void setCurrentCategory(String category) {
        //? if >1.19.4 {
        
        double offset = ((ScrollContentAccessor) this.categoriesScroll).carpetGUI$getScrollOffset();
        double max = ((ScrollContentAccessor) this.categoriesScroll).carpetGUI$getMaxScroll();

        lastCategoryScroll = max == 0 ? 0 : offset / max;
        //?} else {
        /*lastCategoryScroll = ((ScrollContentAccessor) this.categoriesScroll).carpetGUI$getScrollOffset();
        *///?}

        boolean justRefresh = Objects.equals(category, currentCategory);

        double roffset = ((ScrollContentAccessor) this.rulesScroll).carpetGUI$getScrollOffset();
        var rmax = ((ScrollContentAccessor) this.rulesScroll).carpetGUI$getMaxScroll();
        if (justRefresh) {
            //? if >1.19.4 {

            lastRuleListScroll = rmax == 0 ? 0 : roffset / rmax;
            //?} else {
            /*lastCategoryScroll = ((ScrollContentAccessor) this.rulesScroll).carpetGUI$getScrollOffset();
            *///?}

        } else {
            lastRuleListScroll = 0;
        }

        this.currentCategory = category;
        if (!Objects.equals(currentCategory, DefaultCategory.SEARCHING.getName())) {
            lastCategoryBeforeSearching = category;
        }

        if (currentCategoryLabel != null) {
            currentCategoryLabel.text(DefaultCategory.getDisplayName(currentCategory));
        }

        if (categoriesListLayout != null) {
            categoriesListLayout.clearChildren();
            cachedCategories.forEach(c ->
                    categoriesListLayout.child(buildCategoryRow(c)));
        }

        Stream<RuleData> stream = getRuleDataStream();

        rebuildRulesList(stream, "");
        //? if >1.19.4 {
        this.categoriesScroll.scrollTo(lastCategoryScroll);
        if (justRefresh) {
            this.rulesScroll.scrollTo(lastRuleListScroll);
        }
        //?} else {
        /*((ScrollContentAccessor) this.categoriesScroll).carpetGUI$setScrollOffset(lastCategoryScroll);
        if (justRefresh) {
            ((ScrollContentAccessor)this.rulesScroll).carpetGUI$setScrollOffset(lastRuleListScroll);
        }
        *///?}
    }

    private @NotNull Stream<RuleData> getRuleDataStream() {
        Stream<RuleData> stream;
        if (Objects.equals(currentCategory, DefaultCategory.DEFAULT.getName())) {
            stream = cachedCompleteRules.values().stream().filter(r -> {
                String org = r.name;
                return defaultRules.contains(org);
            });
        } else if (Objects.equals(currentCategory, DefaultCategory.FAVORITE.getName())) {
            stream = cachedCompleteRules.values().stream().filter(r -> {
                String org = r.name;

                return favoriteRules.contains(org);
            });
        } else if (Objects.equals(currentCategory, DefaultCategory.MODIFIED.getName())) {
            stream = cachedCompleteRules.values().stream().filter(r -> !r.defaultValue.equals(r.value));
        } else if (Objects.equals(currentCategory, DefaultCategory.GAMERULES.getName())) {
            stream = cachedCompleteRules.values().stream().filter(r -> r.categories.getFirst().getKey().equals("gamerule"));
        } else if (Objects.equals(currentCategory, DefaultCategory.ALL.getName())) {
            stream = cachedCompleteRules.values().stream().filter(r -> !r.categories.getFirst().getKey().equals("gamerule"));
        } else {
            stream = cachedCompleteRules.values().stream().filter(r -> r.categories.stream().anyMatch(e -> Objects.equals(e.getValue(), currentCategory)));
        }
        return stream;
    }

    private void rebuildRulesList(Stream<RuleData> stream, String query) {
        rulesListLayout.clearChildren();
        stream.sorted(Comparator.comparing(rule -> {
            String en = rule.name;
            return en.isEmpty() ? "" : en.toLowerCase().substring(0, 1);
        })).forEach(r -> rulesListLayout.child(new RuleWidget(r, this, query).buildComponent()));
    }

    private FlowLayout buildCategoryRow(String name) {
        boolean selected = Objects.equals(name, currentCategory);

        var row = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        row.surface(Surface.flat(selected ? 0x50060606 : 0x20060606));
        row.padding(Insets.of(4, 4, 6, 0));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.cursorStyle(CursorStyle.HAND);

        var label = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(DefaultCategory.getDisplayName(name));
        label.color(Color.WHITE);
        row.child(label);
        //? if <1.21.9 {
        /*row.mouseDown().subscribe((x, y, btn) -> {
        *///?} else {
        row.mouseDown().subscribe((mouseButtonEvent, btn) -> {
        //?}

            setCurrentCategory(name);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            return true;
        });
        return row;
    }

    public static boolean matchesRule(List<String> parts, String input) {
        if (input == null || input.isEmpty() || parts == null || parts.isEmpty()) return false;
        String lower = input.toLowerCase();
        for (String part : parts) {
            if (part.toLowerCase().contains(lower) || part.contains(input)) return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        if (!this.instantAffect) {
            ScreenUtils.showSaveGroupDialog(this.uiAdapter.rootComponent, dialogOverlay);
        }else {
            super.onClose();
        }
    }
}