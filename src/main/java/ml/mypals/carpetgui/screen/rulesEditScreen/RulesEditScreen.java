package ml.mypals.carpetgui.screen.rulesEditScreen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import ml.mypals.carpetgui.mixin.accessors.ScrollContentAccessor;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.screen.ScreenUtils;
import ml.mypals.carpetgui.screen.ruleGroup.RuleCommand;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroup;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupLoader;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static ml.mypals.carpetgui.CarpetGUI.MOD_ID;
import static ml.mypals.carpetgui.CarpetGUIClient.*;

public class RulesEditScreen extends BaseOwoScreen<FlowLayout> {
    public boolean instantAffect = false;
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
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.blur(10, 10));

        var leftPanel = Containers.verticalFlow(Sizing.fill(66), Sizing.content());

        var searchRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
        searchRow.verticalAlignment(VerticalAlignment.CENTER);
        searchRow.padding(Insets.of(2, 2, 4, 4));
        searchRow.surface(Surface.flat(0x0AAAAAAA));

        var searchIcon = Components.texture(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/search.png"),
                0, 0, 10, 11, 10, 11);
        searchIcon.sizing(Sizing.fixed(10), Sizing.fixed(11));
        searchIcon.id("search-icon");
        searchRow.child(searchIcon);

        searchBox = Components.textBox(Sizing.fill(100));
        searchBox.setMaxLength(100);

        searchBox.onChanged().subscribe(this::onSearch);
        searchBox.focusGained().subscribe(source -> this.onSearch(searchBox.getValue()));
        searchBox.charTyped().subscribe((chr, scanCode) -> {
            if (chr == '\r') {
                this.setCurrentCategory(lastCategoryBeforeSearching);
                return true;
            }
            return false;
        });
        searchRow.child(searchBox);
        leftPanel.child(searchRow);
        leftPanel.allowOverflow();
        rulesListLayout = Containers.verticalFlow(Sizing.fill(99), Sizing.content());
        rulesScroll = Containers.verticalScroll(
                Sizing.fill(), Sizing.fill(), rulesListLayout);
        rulesScroll.surface(Surface.flat(0x19000000));
        rulesScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
        leftPanel.child(rulesScroll);


        var rightPanel = Containers.verticalFlow(Sizing.fill(34), Sizing.fill(100));
        rightPanel.surface(Surface.flat(0x0F060606));
        rightPanel.padding(Insets.of(5));

        currentCategoryLabel = Components.label(Component.nullToEmpty(currentCategory));
        currentCategoryLabel.color(Color.WHITE);
        currentCategoryLabel.margins(Insets.of(2, 6, 4, 0));
        rightPanel.child(currentCategoryLabel);

        categoriesListLayout = Containers.verticalFlow(Sizing.fill(98), Sizing.content());
        categoriesScroll = Containers.verticalScroll(
                Sizing.fill(), Sizing.fill(), categoriesListLayout);
        categoriesScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
        categoriesScroll.surface(Surface.flat(0x19000000));
        categoriesScroll.scrollbarThiccness(10);
        rightPanel.child(categoriesScroll);

        root.child(leftPanel);
        root.child(rightPanel);

        setCurrentCategory(cachedCategories.getFirst());

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

        ScreenKeyboardEvents.afterKeyPress(this).register((screen, key, scancode, modifiers) -> {
            if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && key == GLFW.GLFW_KEY_S) {
                ScreenUtils.showSaveGroupDialog(this.uiAdapter.rootComponent, dialogOverlay);
            }
        });
    }

    private void saveModifiedRulesAsGroup(String groupName) {
        List<RuleData> modifiedRules = cachedRules.stream()
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
            setCurrentCategory(DefaultCategory.SEARCHING.getName());

            rebuildRulesList(
                    cachedRules.stream().filter(r -> {
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
        double offset = ((ScrollContentAccessor) this.categoriesScroll).getScrollOffset();
        double max = ((ScrollContentAccessor) this.categoriesScroll).getMaxScroll();
        lastCategoryScroll = max == 0 ? 0 : offset / max;

        boolean justRefresh = Objects.equals(category, currentCategory);
        if (justRefresh) {
            double roffset = ((ScrollContentAccessor) this.rulesScroll).getScrollOffset();
            double rmax = ((ScrollContentAccessor) this.rulesScroll).getMaxScroll();
            lastRuleListScroll = rmax == 0 ? 0 : roffset / rmax;
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

        if (rulesListLayout == null || Objects.equals(currentCategory, DefaultCategory.SEARCHING.getName())) return;

        Stream<RuleData> stream = getRuleDataStream();

        rebuildRulesList(stream, "");
        this.categoriesScroll.scrollTo(lastCategoryScroll);
        if (justRefresh) {
            this.rulesScroll.scrollTo(lastRuleListScroll);
        }
    }

    private @NotNull Stream<RuleData> getRuleDataStream() {
        Stream<RuleData> stream;
        if (Objects.equals(currentCategory, DefaultCategory.DEFAULT.getName())) {
            stream = cachedRules.stream().filter(r -> {
                String org = r.name;
                return defaultRules.contains(org);
            });
        } else if (Objects.equals(currentCategory, DefaultCategory.FAVORITE.getName())) {
            stream = cachedRules.stream().filter(r -> {
                String org = r.name;

                return favoriteRules.contains(org);
            });
        } else if (Objects.equals(currentCategory, DefaultCategory.MODIFIED.getName())) {
            stream = cachedRules.stream().filter(r -> !r.defaultValue.equals(r.value));
        } else if (Objects.equals(currentCategory, DefaultCategory.GAMERULES.getName())) {
            stream = cachedRules.stream().filter(r -> r.categories.getFirst().getKey().equals("gamerule"));
        } else if (Objects.equals(currentCategory, DefaultCategory.ALL.getName())) {
            stream = cachedRules.stream().filter(r -> !r.categories.getFirst().getKey().equals("gamerule"));
        } else {
            stream = cachedRules.stream().filter(r -> r.categories.stream().anyMatch(e -> Objects.equals(e.getValue(), currentCategory)));
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

        var row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        row.surface(Surface.flat(selected ? 0x50060606 : 0x20060606));
        row.padding(Insets.of(4, 4, 6, 0));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.cursorStyle(CursorStyle.HAND);

        var label = Components.label(DefaultCategory.getDisplayName(name));
        label.color(Color.WHITE);
        row.child(label);

        row.mouseDown().subscribe((mx, my, btn) -> {
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
        } else {
            super.onClose();
        }
    }
}