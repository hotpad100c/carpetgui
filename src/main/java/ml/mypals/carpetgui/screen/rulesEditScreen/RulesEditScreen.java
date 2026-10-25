package ml.mypals.carpetgui.screen.rulesEditScreen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.screen.ScreenTabBar;
import ml.mypals.carpetgui.screen.ScreenUtils;
import ml.mypals.carpetgui.screen.SettingsPanel;
import ml.mypals.carpetgui.screen.ruleGroup.RuleCommand;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroup;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupLoader;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import ml.mypals.carpetgui.ui.base.BaseOwoScreen;
import ml.mypals.carpetgui.ui.component.LabelComponent;
import ml.mypals.carpetgui.ui.component.TextBoxComponent;
import ml.mypals.carpetgui.ui.component.TextureComponent;
import ml.mypals.carpetgui.ui.component.UIComponents;
import ml.mypals.carpetgui.ui.container.FlowLayout;
import ml.mypals.carpetgui.ui.container.OverlayContainer;
import ml.mypals.carpetgui.ui.container.ScrollContainer;
import ml.mypals.carpetgui.ui.container.UIContainers;
import ml.mypals.carpetgui.ui.core.Color;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.HorizontalAlignment;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIAdapter;
import ml.mypals.carpetgui.ui.core.Positioning;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.core.VerticalAlignment;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
//?}
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RulesEditScreen extends BaseOwoScreen<FlowLayout> {
   private static final Logger log = LoggerFactory.getLogger(RulesEditScreen.class);
   public boolean instantAffect;
   private FlowLayout saveDialog;
   private OverlayContainer<FlowLayout> dialogOverlay;
   public double lastRuleListScroll = (double)0.0F;
   // Combined with AND; empty means "all". Static so the selection survives reopening the screen.
   private static final LinkedHashSet<String> activeFilters = new LinkedHashSet();
   private String lastQuery = "";
   private FlowLayout leftPanel;
   private FlowLayout rightPanel;
   private FlowLayout rulesListLayout;
   private FlowLayout categoriesListLayout;
   private LabelComponent currentCategoryLabel;
   private TextBoxComponent searchBox;
   private ScrollContainer<FlowLayout> categoriesScroll;
   private ScrollContainer<FlowLayout> rulesScroll;
   private final Map<String, RuleWidget> ruleWidgets = new HashMap();
   private final Map<String, FlowLayout> categoryRows = new HashMap();
   private final Map<String, LabelComponent> categoryMarks = new HashMap();

   public RulesEditScreen(boolean instantAffect) {
      this.instantAffect = instantAffect;
   }

   protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
      return OwoUIAdapter.<FlowLayout>create(this, UIContainers::verticalFlow);
   }

   protected void build(FlowLayout root) {
      ScreenTabBar.build(this.buildMain(root), ScreenTabBar.Tab.RULES, this::applyPanelWidths);
   }

   protected FlowLayout buildMain(FlowLayout root) {
      Map.Entry<FlowLayout, FlowLayout> master = ScreenUtils.makeMasterContainer(this.width, this.height, root);
      ScreenUtils.DialogResult dialogResult = ScreenUtils.createSaveGroupDialog(this::saveModifiedRulesAsGroup, (ingnored) -> {
         ScreenUtils.hideSaveDialog((FlowLayout)this.uiAdapter.rootComponent, this.dialogOverlay);
         if (!this.instantAffect) {
            this.instantAffect = true;
            Minecraft.getInstance().setScreenAndShow(new RuleGroupScreen());
         }

      });
      this.saveDialog = dialogResult.dialog();
      this.dialogOverlay = dialogResult.overlay();
      this.leftPanel = UIContainers.verticalFlow(Sizing.fill(100 - CarpetGUIConfigManager.categoryPanelWidth), Sizing.fill(100));
      leftPanel.padding(Insets.of(5));
      FlowLayout searchRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fill(10));
      searchRow.verticalAlignment(VerticalAlignment.CENTER);
      searchRow.padding(Insets.of(2, 2, 4, 4));
      searchRow.surface(Surface.flat(178956970));
      TextureComponent searchIcon = UIComponents.texture(Identifier.fromNamespaceAndPath("carpetgui", "ui/search.png"), 0, 0, 10, 11, 10, 11);
      searchIcon.sizing(Sizing.fixed(10), Sizing.fixed(11));
      searchRow.child(searchIcon);
      this.searchBox = UIComponents.textBox(Sizing.fill(100));
      this.searchBox.setMaxLength(100);
      this.searchBox.setFocused(true);
      this.searchBox.onChanged().subscribe(this::onSearch);
      this.searchBox.carpetGUI$focusGained().subscribe((FocusGained)(source) -> this.onSearch(this.searchBox.getValue()));
      searchRow.child(this.searchBox);
      leftPanel.child(searchRow);
      this.rulesListLayout = UIContainers.verticalFlow(Sizing.fill(99), Sizing.content());
      this.rulesScroll = UIContainers.<FlowLayout>verticalScroll(Sizing.fill(100), Sizing.fill(90), this.rulesListLayout);
      this.rulesScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
      leftPanel.child(this.rulesScroll);
      this.rightPanel = UIContainers.verticalFlow(Sizing.fill(CarpetGUIConfigManager.categoryPanelWidth), Sizing.fill(100));
      rightPanel.padding(Insets.of(5));
      this.currentCategoryLabel = UIComponents.label(Component.literal(""));
      this.currentCategoryLabel.color(Color.WHITE);
      this.currentCategoryLabel.carpetGUI$margins(Insets.of(2, 6, 4, 0));
      rightPanel.child(this.currentCategoryLabel);
      this.categoriesListLayout = UIContainers.verticalFlow(Sizing.fill(98), Sizing.content());
      this.categoriesScroll = UIContainers.<FlowLayout>verticalScroll(Sizing.fill(100), Sizing.fill(100), this.categoriesListLayout);
      this.categoriesScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
      this.categoriesScroll.surface(Surface.flat(419430400));
      this.categoriesScroll.scrollbarThiccness(10);
      rightPanel.child(this.categoriesScroll);
      ((FlowLayout)master.getValue()).child(leftPanel);
      ((FlowLayout)master.getValue()).child(rightPanel);
      root.child((UIComponent)master.getKey());
      this.rebuildCategoriesList();
      this.remountRules(false);
      //? if <1.21.9 {
      /*ScreenKeyboardEvents.afterKeyPress(this).register((screen, key, scancode, modifiers) -> {
         if ((modifiers & 2) != 0 && key == 83) {
            ScreenUtils.showSaveGroupDialog((FlowLayout)this.uiAdapter.rootComponent, this.dialogOverlay);
         }
      });
      *///?} else {
      ScreenKeyboardEvents.afterKeyPress(this).register((ScreenKeyboardEvents.AfterKeyPress)(screen, key) -> {
         if (key.hasControlDown() && key.key() == InputConstants.KEY_S) {
            ScreenUtils.showSaveGroupDialog((FlowLayout)this.uiAdapter.rootComponent, this.dialogOverlay);
         }
      });
      //?}
      return (FlowLayout)master.getKey();
   }

   //? if <1.21.9 {
   /*@Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if ((Screen.hasControlDown() || (modifiers & 2) != 0) && keyCode == 83) {
         ScreenUtils.showSaveGroupDialog((FlowLayout)this.uiAdapter.rootComponent, this.dialogOverlay);
         return true;
      }
      return super.keyPressed(keyCode, scanCode, modifiers);
   }
   *///?} else {
   @Override
   public boolean keyPressed(KeyEvent input) {
      if (input.hasControlDown() && input.key() == InputConstants.KEY_S) {
         ScreenUtils.showSaveGroupDialog((FlowLayout)this.uiAdapter.rootComponent, this.dialogOverlay);
         return true;
      }
      return super.keyPressed(input);
   }
   //?}

   private void saveModifiedRulesAsGroup(String groupName) {
      List<RuleData> modifiedRules = CarpetGUIClient.cachedCompleteRules.values().stream().filter((r) -> !Objects.equals(r.defaultValue, r.value) || CarpetGUIClient.defaultRules.contains(r.name)).toList();
      if (!modifiedRules.isEmpty()) {
         List<RuleCommand> commands = modifiedRules.stream().map((rule) -> {
            String prefix = rule.isGamerule ? "gamerule" : rule.manager;
            return new RuleCommand(-1, prefix, rule.name, rule.value, CarpetGUIClient.defaultRules.contains(rule.name), true);
         }).toList();
         Path path = RuleGroupLoader.GROUPS_DIR.resolve(groupName + ".txt");
         RuleGroup group = new RuleGroup(groupName, path, commands);
         boolean success = RuleGroupLoader.save(group);
         if (success) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.RESPAWN_ANCHOR_CHARGE, 5.0F));
         }

      }
   }

   public void onSearch(String input) {
      if (Objects.equals(input, this.lastQuery)) {
         return;
      }

      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      // Keep the scroll position while refining a query; start from the top otherwise.
      boolean refining = !input.isEmpty() && !this.lastQuery.isEmpty();
      this.remountRules(refining);
   }

   public void refreshScreen() {
      this.remountRules(true);
   }

   public boolean isFilterActive(DefaultCategory filter) {
      return activeFilters.contains(filter.getName());
   }

   private void applyPanelWidths() {
      this.leftPanel.sizing(Sizing.fill(100 - CarpetGUIConfigManager.categoryPanelWidth), Sizing.fill(100));
      this.rightPanel.sizing(Sizing.fill(CarpetGUIConfigManager.categoryPanelWidth), Sizing.fill(100));
   }

   private void toggleFilter(String name, boolean additive) {
      if (Objects.equals(name, RulesEditScreen.DefaultCategory.ALL.getName())) {
         activeFilters.clear();
      } else if (additive) {
         if (!activeFilters.remove(name)) {
            activeFilters.add(name);
         }
      } else if (activeFilters.size() == 1 && activeFilters.contains(name)) {
         activeFilters.clear();
      } else {
         activeFilters.clear();
         activeFilters.add(name);
      }

      this.updateCategoryHighlights();
      this.remountRules(false);
   }
   private void remountRules(boolean keepScroll) {
      double roffset = this.rulesScroll.scrollOffset();
      int rmax = this.rulesScroll.maxScroll();
      this.lastRuleListScroll = keepScroll && rmax != 0 ? roffset / (double)rmax : (double)0.0F;
      String query = this.searchBox.getValue();
      this.lastQuery = query;
      Predicate<RuleData> visible = this.filterPredicate(query);
      if (!query.isEmpty()) {
         visible = visible.and((r) -> matchesRule(searchParts(r), query));
      }

      List<RuleData> rules = CarpetGUIClient.cachedCompleteRules.values().stream().filter(visible).toList();
      this.rebuildRulesList(rules.stream(), query);
      this.updateFilterLabel(query, rules.size());
      this.rulesScroll.scrollTo(this.lastRuleListScroll);
   }

   private Predicate<RuleData> filterPredicate(String query) {
      if (activeFilters.isEmpty()) {
         return query.isEmpty() ? predicateFor(RulesEditScreen.DefaultCategory.ALL.getName()) : (r) -> true;
      } else {
         Predicate<RuleData> combined = (r) -> true;

         for(String filter : activeFilters) {
            combined = combined.and(predicateFor(filter));
         }

         return combined;
      }
   }

   private static Predicate<RuleData> predicateFor(String filter) {
      if (Objects.equals(filter, RulesEditScreen.DefaultCategory.DEFAULT.getName())) {
         return (r) -> CarpetGUIClient.defaultRules.contains(r.name);
      } else if (Objects.equals(filter, RulesEditScreen.DefaultCategory.FAVORITE.getName())) {
         return (r) -> CarpetGUIClient.favoriteRules.contains(r.name);
      } else if (Objects.equals(filter, RulesEditScreen.DefaultCategory.MODIFIED.getName())) {
         return (r) -> !r.defaultValue.equals(r.value);
      } else if (Objects.equals(filter, RulesEditScreen.DefaultCategory.GAMERULES.getName())) {
         return (r) -> !r.categories.isEmpty() && ((String)((Map.Entry)r.categories.get(0)).getKey()).equals("gamerule");
      } else if (Objects.equals(filter, RulesEditScreen.DefaultCategory.ALL.getName())) {
         return (r) -> !r.categories.isEmpty() && !((String)((Map.Entry)r.categories.get(0)).getKey()).equals("gamerule");
      } else {
         return (r) -> r.categories.stream().anyMatch((e) -> Objects.equals(e.getValue(), filter));
      }
   }

   private static List<String> searchParts(RuleData r) {
      List<String> parts = new ArrayList();
      parts.add(r.name);
      parts.add(r.localName);
      parts.add(r.description);
      parts.add(r.localDescription);
      parts.addAll(r.categories.stream().map(Map.Entry::getKey).toList());
      parts.addAll(r.categories.stream().map(Map.Entry::getValue).toList());
      return parts;
   }

   private void updateFilterLabel(String query, int count) {
      MutableComponent text;
      if (activeFilters.isEmpty()) {
         text = query.isEmpty() ? RulesEditScreen.DefaultCategory.getDisplayName(RulesEditScreen.DefaultCategory.ALL.getName()).copy() : Component.translatable("gui.category.searching");
      } else {
         text = Component.literal("");
         boolean first = true;

         for(String filter : activeFilters) {
            if (!first) {
               text.append(" + ");
            }

            text.append(RulesEditScreen.DefaultCategory.getDisplayName(filter));
            first = false;
         }
      }

      if (!query.isEmpty()) {
         text.append(" - " + count);
      }

      this.currentCategoryLabel.text(text);
   }

   private void updateCategoryHighlights() {
      this.categoryRows.forEach((name, row) -> {
         boolean selected = Objects.equals(name, RulesEditScreen.DefaultCategory.ALL.getName()) ? activeFilters.isEmpty() : activeFilters.contains(name);
         row.surface(Surface.flat(selected ? 1342572038 : 537265670));
         this.categoryMarks.get(name).text(Component.literal(selected ? "✔" : ""));
      });
   }

   private void rebuildCategoriesList() {
      // Drop filters this server doesn't know, otherwise the list would be stuck empty.
      activeFilters.retainAll(CarpetGUIClient.cachedCategories);
      this.categoryRows.clear();
      this.categoryMarks.clear();
      List<UIComponent> rows = new ArrayList();
      CarpetGUIClient.cachedCategories.forEach((c) -> rows.add(this.buildCategoryRow(c)));
      this.categoriesListLayout.clearChildren();
      this.categoriesListLayout.children(rows);
      this.updateCategoryHighlights();
   }

   private void rebuildRulesList(Stream<RuleData> stream, String query) {
      List<UIComponent> rows = stream.sorted(Comparator.comparing((rule) -> {
         String en = rule.name;
         return en.isEmpty() ? "" : en.toLowerCase().substring(0, 1);
      })).map((r) -> {
         RuleWidget widget = this.getRuleWidget(r);
         widget.applyQuery(query);
         return (UIComponent)widget.getRow();
      }).toList();
      // Mount in one batch: child() per row would re-layout the whole list each time.
      this.rulesListLayout.clearChildren();
      this.rulesListLayout.children(rows);
   }

   private RuleWidget getRuleWidget(RuleData rule) {
      RuleWidget widget = this.ruleWidgets.get(rule.name);
      // cachedCompleteRules may have been replaced since the widget was built
      if (widget == null || widget.getRuleData() != rule) {
         widget = new RuleWidget(rule, this);
         this.ruleWidgets.put(rule.name, widget);
      }

      return widget;
   }

   private FlowLayout buildCategoryRow(String name) {
      FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
      row.padding(Insets.of(4, 4, 6, 6));
      row.verticalAlignment(VerticalAlignment.CENTER);
      row.carpetGUI$cursorStyle(CursorStyle.HAND);
      LabelComponent label = UIComponents.label(RulesEditScreen.DefaultCategory.getDisplayName(name));
      label.color(Color.WHITE);
      row.child(label);
      LabelComponent mark = UIComponents.label(Component.literal(""));
      mark.color(Color.WHITE);
      mark.carpetGUI$positioning(Positioning.relative(100, 50));
      row.child(mark);
      row.carpetGUI$tooltip(Component.translatable("gui.category.multiselect_hint"));
      this.categoryRows.put(name, row);
      this.categoryMarks.put(name, mark);
      row.carpetGUI$mouseDown().subscribe((MouseDown)(mouseButtonEvent, btn) -> {
         boolean additive = ScreenUtils.isKeyDown(340) || ScreenUtils.isKeyDown(344) || ScreenUtils.isKeyDown(341) || ScreenUtils.isKeyDown(345);
         this.toggleFilter(name, additive);
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         return true;
      });
      return row;
   }

   public static boolean matchesRule(List<String> parts, String input) {
      if (input != null && !input.isEmpty() && parts != null && !parts.isEmpty()) {
         String lower = input.toLowerCase();

         for(String part : parts) {
            if (part.toLowerCase().contains(lower) || part.contains(input)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void onClose() {
      if (SettingsPanel.isOpen((FlowLayout)this.uiAdapter.rootComponent)) {
         SettingsPanel.close();
      } else if (!this.instantAffect) {
         ScreenUtils.showSaveGroupDialog((FlowLayout)this.uiAdapter.rootComponent, this.dialogOverlay);
      } else {
         super.onClose();
      }

   }

   public static enum DefaultCategory {
      ALL("all"),
      DEFAULT("default"),
      GAMERULES("gamerules"),
      FAVORITE("favorite"),
      MODIFIED("modified");

      private final String name;

      private DefaultCategory(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public static Component getDisplayName(String name) {
         for(DefaultCategory df : values()) {
            if (df.getName().equals(name)) {
               return Component.translatable("gui.category." + name);
            }
         }

         return Component.translatable(name);
      }
   }
}
