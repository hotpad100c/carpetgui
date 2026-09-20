package ml.mypals.carpetgui.screen.rulesEditScreen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.screen.ScreenTabBar;
import ml.mypals.carpetgui.screen.ScreenUtils;
import ml.mypals.carpetgui.screen.ruleGroup.RuleCommand;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroup;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupLoader;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
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
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.core.VerticalAlignment;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
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
   public double lastCategoryScroll = (double)0.0F;
   public double lastRuleListScroll = (double)0.0F;
   public String currentCategory = "unknown";
   public String lastCategoryBeforeSearching;
   public boolean searching;
   private FlowLayout rulesListLayout;
   private FlowLayout categoriesListLayout;
   private LabelComponent currentCategoryLabel;
   private TextBoxComponent searchBox;
   private ScrollContainer<FlowLayout> categoriesScroll;
   private ScrollContainer<FlowLayout> rulesScroll;

   public RulesEditScreen(boolean instantAffect) {
      this.lastCategoryBeforeSearching = this.currentCategory;
      this.searching = false;
      this.instantAffect = instantAffect;
   }

   protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
      return OwoUIAdapter.<FlowLayout>create(this, UIContainers::verticalFlow);
   }

   protected void build(FlowLayout root) {
      ScreenTabBar.build(this.buildMain(root), ScreenTabBar.Tab.RULES);
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
      FlowLayout leftPanel = UIContainers.verticalFlow(Sizing.fill(66), Sizing.fill(100));
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
      FlowLayout rightPanel = UIContainers.verticalFlow(Sizing.fill(34), Sizing.fill(100));
      rightPanel.padding(Insets.of(5));
      this.currentCategoryLabel = UIComponents.label(Component.nullToEmpty(this.currentCategory));
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
      root.horizontalAlignment(HorizontalAlignment.CENTER);
      root.verticalAlignment(VerticalAlignment.CENTER);
      root.child((UIComponent)master.getKey());
      this.setCurrentCategory(RulesEditScreen.DefaultCategory.ALL.getName());
      //? if <1.21.9 {
      /*ScreenKeyboardEvents.afterKeyPress(this).register((screen, key, scancode, modifiers) -> {
         if ((modifiers & 2) != 0 && key == 83) {
            ScreenUtils.showSaveGroupDialog((FlowLayout)this.uiAdapter.rootComponent, this.dialogOverlay);
         }
      });
      *///?} else {
      ScreenKeyboardEvents.afterKeyPress(this).register((ScreenKeyboardEvents.AfterKeyPress)(screen, key) -> {
         if ((key.modifiers() & 2) != 0 && key.key() == 83) {
            ScreenUtils.showSaveGroupDialog((FlowLayout)this.uiAdapter.rootComponent, this.dialogOverlay);
         }

      });
      //?}
      return (FlowLayout)master.getKey();
   }

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
      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      if (!Objects.equals(this.currentCategory, RulesEditScreen.DefaultCategory.SEARCHING.getName())) {
         this.lastCategoryBeforeSearching = this.currentCategory;
      }

      if (input.isEmpty()) {
         this.searching = false;
         this.setCurrentCategory(this.lastCategoryBeforeSearching);
      } else {
         this.searching = true;
         double roffset = this.rulesScroll.scrollOffset();
         int rmax = this.rulesScroll.maxScroll();
         this.setCurrentCategory(RulesEditScreen.DefaultCategory.SEARCHING.getName());
         this.rebuildRulesList(CarpetGUIClient.cachedCompleteRules.values().stream().filter((r) -> {
            List<String> parts = new ArrayList();
            parts.add(r.name);
            parts.add(r.localName);
            parts.add(r.description);
            parts.add(r.localDescription);
            parts.addAll(r.categories.stream().map(Map.Entry::getKey).toList());
            parts.addAll(r.categories.stream().map(Map.Entry::getValue).toList());
            return matchesRule(parts, input);
         }), this.searchBox.getValue());
         this.rulesScroll.scrollTo(this.lastRuleListScroll);
      }

   }

   public void refreshScreen() {
      if (Objects.equals(this.currentCategory, RulesEditScreen.DefaultCategory.SEARCHING.getName())) {
         this.onSearch(this.searchBox.getValue());
      } else {
         this.setCurrentCategory(this.currentCategory);
      }

   }

   public void setCurrentCategory(String category) {
      double offset = this.categoriesScroll.scrollOffset();
      double max = (double)this.categoriesScroll.maxScroll();
      this.lastCategoryScroll = max == (double)0.0F ? (double)0.0F : offset / max;
      boolean justRefresh = Objects.equals(category, this.currentCategory);
      double roffset = this.rulesScroll.scrollOffset();
      int rmax = this.rulesScroll.maxScroll();
      if (justRefresh) {
         this.lastRuleListScroll = rmax == 0 ? (double)0.0F : roffset / (double)rmax;
      } else {
         this.lastRuleListScroll = (double)0.0F;
      }

      this.currentCategory = category;
      if (!Objects.equals(this.currentCategory, RulesEditScreen.DefaultCategory.SEARCHING.getName())) {
         this.lastCategoryBeforeSearching = category;
      }

      if (this.currentCategoryLabel != null) {
         this.currentCategoryLabel.text(RulesEditScreen.DefaultCategory.getDisplayName(this.currentCategory));
      }

      if (this.categoriesListLayout != null) {
         this.categoriesListLayout.clearChildren();
         CarpetGUIClient.cachedCategories.forEach((c) -> this.categoriesListLayout.child(this.buildCategoryRow(c)));
      }

      Stream<RuleData> stream = this.getRuleDataStream();
      this.rebuildRulesList(stream, "");
      this.categoriesScroll.scrollTo(this.lastCategoryScroll);
      if (justRefresh) {
         this.rulesScroll.scrollTo(this.lastRuleListScroll);
      }

   }

   private @NotNull Stream<RuleData> getRuleDataStream() {
      Stream<RuleData> stream;
      if (Objects.equals(this.currentCategory, RulesEditScreen.DefaultCategory.DEFAULT.getName())) {
         stream = CarpetGUIClient.cachedCompleteRules.values().stream().filter((r) -> {
            String org = r.name;
            return CarpetGUIClient.defaultRules.contains(org);
         });
      } else if (Objects.equals(this.currentCategory, RulesEditScreen.DefaultCategory.FAVORITE.getName())) {
         stream = CarpetGUIClient.cachedCompleteRules.values().stream().filter((r) -> {
            String org = r.name;
            return CarpetGUIClient.favoriteRules.contains(org);
         });
      } else if (Objects.equals(this.currentCategory, RulesEditScreen.DefaultCategory.MODIFIED.getName())) {
         stream = CarpetGUIClient.cachedCompleteRules.values().stream().filter((r) -> !r.defaultValue.equals(r.value));
      } else if (Objects.equals(this.currentCategory, RulesEditScreen.DefaultCategory.GAMERULES.getName())) {
         stream = CarpetGUIClient.cachedCompleteRules.values().stream().filter((r) -> ((String)((Map.Entry)r.categories.getFirst()).getKey()).equals("gamerule"));
      } else if (Objects.equals(this.currentCategory, RulesEditScreen.DefaultCategory.ALL.getName())) {
         stream = CarpetGUIClient.cachedCompleteRules.values().stream().filter((r) -> !((String)((Map.Entry)r.categories.getFirst()).getKey()).equals("gamerule"));
      } else {
         stream = CarpetGUIClient.cachedCompleteRules.values().stream().filter((r) -> r.categories.stream().anyMatch((e) -> Objects.equals(e.getValue(), this.currentCategory)));
      }

      return stream;
   }

   private void rebuildRulesList(Stream<RuleData> stream, String query) {
      this.rulesListLayout.clearChildren();
      stream.sorted(Comparator.comparing((rule) -> {
         String en = rule.name;
         return en.isEmpty() ? "" : en.toLowerCase().substring(0, 1);
      })).forEach((r) -> this.rulesListLayout.child((new RuleWidget(r, this, query)).buildComponent()));
   }

   private FlowLayout buildCategoryRow(String name) {
      boolean selected = Objects.equals(name, this.currentCategory);
      FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
      row.surface(Surface.flat(selected ? 1342572038 : 537265670));
      row.padding(Insets.of(4, 4, 6, 0));
      row.verticalAlignment(VerticalAlignment.CENTER);
      row.carpetGUI$cursorStyle(CursorStyle.HAND);
      LabelComponent label = UIComponents.label(RulesEditScreen.DefaultCategory.getDisplayName(name));
      label.color(Color.WHITE);
      row.child(label);
      row.carpetGUI$mouseDown().subscribe((MouseDown)(mouseButtonEvent, btn) -> {
         this.setCurrentCategory(name);
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
      if (!this.instantAffect) {
         ScreenUtils.showSaveGroupDialog((FlowLayout)this.uiAdapter.rootComponent, this.dialogOverlay);
      } else {
         super.onClose();
      }

   }

   public static enum DefaultCategory {
      ALL("all"),
      SEARCHING("searching"),
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

      private static DefaultCategory[] $values() {
         return new DefaultCategory[]{ALL, SEARCHING, DEFAULT, GAMERULES, FAVORITE, MODIFIED};
      }
   }
}
