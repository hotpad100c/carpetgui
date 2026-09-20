package ml.mypals.carpetgui.screen.rulesEditScreen;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.screen.ScreenUtils;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import ml.mypals.carpetgui.ui.component.DropdownComponent;
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
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.VerticalAlignment;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class RuleWidget {
   private final RuleData ruleData;
   private final RulesEditScreen screen;
   private final String orgName;
   private final boolean isTrueFalseRule;
   private String query;
   private boolean isGamerule;
   private boolean isLocked;
   private boolean isFavorited;
   private boolean currentBoolValue;

   public RuleWidget(RuleData ruleData, RulesEditScreen screen, String query) {
      this(ruleData, screen);
      this.query = query;
   }

   public RuleWidget(RuleData ruleData, RulesEditScreen screen) {
      this.query = "";
      this.ruleData = ruleData;
      this.screen = screen;
      this.orgName = ruleData.name;
      this.isGamerule = ruleData.isGamerule;
      this.isLocked = CarpetGUIClient.defaultRules.contains(this.orgName);
      this.isFavorited = CarpetGUIClient.favoriteRules.contains(this.orgName);
      this.currentBoolValue = ruleData.value.equalsIgnoreCase("true");
      this.isTrueFalseRule = ruleData.suggestions.size() == 2 && ((Set)ruleData.suggestions.stream().map(String::toLowerCase).collect(Collectors.toSet())).equals(Set.of("true", "false"));
   }

   public FlowLayout buildComponent() {
      FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(25));
      row.surface(Surface.flat(-1727855869).and(Surface.outline(301989887)));
      row.padding(Insets.of(2, 2, 5, 5));
      row.verticalAlignment(VerticalAlignment.CENTER);
      FlowLayout leftCol = UIContainers.verticalFlow(Sizing.fill(50), Sizing.fill(100));
      leftCol.verticalAlignment(VerticalAlignment.CENTER);
      String displayName = this.ruleData.localName;
      LabelComponent nameLabel = UIComponents.label(ScreenUtils.highlight(displayName + " : ", this.query));
      nameLabel.color(Color.WHITE);
      nameLabel.tooltip(ScreenUtils.buildTooltip(this.ruleData, this.query));
      leftCol.child(nameLabel);
      StringBuilder cats = new StringBuilder();

      for(String c : this.ruleData.categories.stream().map(Map.Entry::getValue).toList()) {
         cats.append(c).append(" | ");
      }

      if (cats.length() > 3) {
         cats.setLength(cats.length() - 3);
      }

      LabelComponent catsLabel = UIComponents.label(ScreenUtils.highlight(cats.toString(), this.query).copy().withStyle(ChatFormatting.BLUE));
      catsLabel.color(Color.ofArgb(-5592406));
      leftCol.child(catsLabel);
      row.child(leftCol);
      FlowLayout rightCol = UIContainers.horizontalFlow(Sizing.fill(50), Sizing.fill(100));
      rightCol.horizontalAlignment(HorizontalAlignment.RIGHT);
      rightCol.verticalAlignment(VerticalAlignment.TOP);
      rightCol.gap(4);
      if (this.isTrueFalseRule) {
         rightCol.child(this.buildBoolToggle());
      } else {
         this.buildTextInput(rightCol);
      }

      if (!Objects.equals(this.ruleData.value, this.ruleData.defaultValue)) {
         rightCol.child(ScreenUtils.buildSpriteToggle(ScreenUtils.RESET, 10, 11, (wrapper) -> {
            String cmd = !this.isGamerule && this.isLocked ? this.ruleData.manager + " setDefault " : this.ruleData.manager + " ";
            this.sendCommand(cmd + this.orgName + " " + this.ruleData.defaultValue);
            this.ruleData.value = this.ruleData.defaultValue;
            this.screen.refreshScreen();
         }));
      }

      if (!this.isGamerule) {
         rightCol.child(ScreenUtils.buildSpriteToggle(this.isLocked ? ScreenUtils.LOCK_ON : ScreenUtils.LOCK_OFF, 10, 11, (wrapper) -> {
            this.isLocked = !this.isLocked;
            if (!this.isLocked) {
               this.sendCommand(this.ruleData.manager + " removeDefault " + this.orgName);
            }

            String cmd = this.isLocked ? this.ruleData.manager + " setDefault " : this.ruleData.manager + " ";
            this.sendCommand(cmd + this.orgName + " " + this.ruleData.value);
            if (this.isLocked) {
               CarpetGUIClient.defaultRules.add(this.orgName);
            } else {
               CarpetGUIClient.defaultRules.remove(this.orgName);
            }

            if (Objects.equals(this.screen.currentCategory, RulesEditScreen.DefaultCategory.DEFAULT.getName())) {
               this.screen.setCurrentCategory(RulesEditScreen.DefaultCategory.DEFAULT.getName());
            }

            ScreenUtils.swapTexture(wrapper, this.isLocked ? ScreenUtils.LOCK_ON : ScreenUtils.LOCK_OFF, 10, 11);
            this.screen.refreshScreen();
         }));
      }

      rightCol.child(ScreenUtils.buildSpriteToggle(this.isFavorited ? ScreenUtils.LOVE_ON : ScreenUtils.LOVE_OFF, 10, 11, (wrapper) -> {
         this.isFavorited = !this.isFavorited;
         if (this.isFavorited) {
            CarpetGUIConfigManager.addFavoriteRule(this.orgName);
            CarpetGUIClient.favoriteRules.add(this.orgName);
         } else {
            CarpetGUIConfigManager.removeFavoriteRule(this.orgName);
            CarpetGUIClient.favoriteRules.remove(this.orgName);
         }

         if (Objects.equals(this.screen.currentCategory, RulesEditScreen.DefaultCategory.FAVORITE.getName())) {
            this.screen.setCurrentCategory(RulesEditScreen.DefaultCategory.FAVORITE.getName());
         }

         ScreenUtils.swapTexture(wrapper, this.isFavorited ? ScreenUtils.LOVE_ON : ScreenUtils.LOVE_OFF, 10, 11);
      }));
      row.child(rightCol);
      return row;
   }

   private FlowLayout buildBoolToggle() {
      FlowLayout wrapper = UIContainers.horizontalFlow(Sizing.fixed(30), Sizing.fixed(13));
      wrapper.carpetGUI$cursorStyle(CursorStyle.HAND);
      wrapper.child(ScreenUtils.makeTexture(this.currentBoolValue ? ScreenUtils.TRUE_TEX : ScreenUtils.FALSE_TEX, 30, 13));
      wrapper.carpetGUI$mouseDown().subscribe((MouseDown)(mouseButtonEvent, btn) -> {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         this.currentBoolValue = !this.currentBoolValue;
         String var10001 = this.ruleData.manager;
         this.sendCommand(var10001 + " " + this.orgName + " " + this.currentBoolValue);
         this.ruleData.value = this.currentBoolValue ? "true" : "false";
         ScreenUtils.swapTexture(wrapper, this.currentBoolValue ? ScreenUtils.TRUE_TEX : ScreenUtils.FALSE_TEX, 30, 13);
         this.screen.refreshScreen();
         return true;
      });
      return wrapper;
   }

   private void buildTextInput(FlowLayout rightCol) {
      FlowLayout content = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
      TextBoxComponent box = UIComponents.textBox(Sizing.fill(100));
      box.setMaxLength(114514);
      DropdownComponent dropdown = UIComponents.dropdown(Sizing.fill(100));
      dropdown.closeWhenNotHovered(false);
      box.setSuggestion(this.ruleData.value);

      for(String suggestion : this.ruleData.suggestions) {
         dropdown.button(new net.minecraft.network.chat.TextComponent(suggestion), (d) -> {
            box.setSuggestion("");
            box.setValue(suggestion);
            this.sendCommand(this.ruleData.manager + " " + this.orgName + " " + suggestion);
            this.ruleData.value = suggestion;
            this.screen.refreshScreen();
         });
      }

      box.carpetGUI$focusGained().subscribe((FocusGained)(focusSource) -> {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         box.setSuggestion("");
      });
      box.carpetGUI$focusLost().subscribe((FocusLost)() -> {
         String val = box.getValue();
         if (!val.isEmpty()) {
            this.sendCommand(this.ruleData.manager + " " + this.orgName + " " + val);
            this.ruleData.value = val;
            box.setValue("");
            box.setSuggestion(this.ruleData.value);
            this.screen.refreshScreen();
         }

      });
      content.child(box);
      if (!this.ruleData.suggestions.isEmpty()) {
         content.child(dropdown);
      }

      FlowLayout wrapper = UIContainers.verticalFlow(Sizing.fixed(70), Sizing.fill(100));
      wrapper.verticalAlignment(VerticalAlignment.CENTER);
      ScrollContainer<FlowLayout> scroll = UIContainers.<FlowLayout>verticalScroll(Sizing.fill(100), Sizing.fill(100), content);
      scroll.scrollbarThiccness(5);
      wrapper.child(scroll);
      rightCol.child(wrapper);
   }

   private void sendCommand(String cmd) {
      ClientPacketListener conn = Minecraft.getInstance().getConnection();
      if (this.screen.instantAffect && conn != null) {
         //? if <1.19 {
         conn.send(new net.minecraft.network.protocol.game.ServerboundChatPacket("/" + cmd));
         //?} else {
         /*conn.sendCommand(cmd);
         *///?}
      }

   }
}
