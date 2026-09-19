package ml.mypals.carpetgui.screen;

import ml.mypals.carpetgui.network.client.CarpetGUIClientPacketHandler;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
import ml.mypals.carpetgui.screen.ruleStack.RuleStackScreen;
import ml.mypals.carpetgui.ui.component.LabelComponent;
import ml.mypals.carpetgui.ui.component.UIComponents;
import ml.mypals.carpetgui.ui.container.FlowLayout;
import ml.mypals.carpetgui.ui.container.UIContainers;
import ml.mypals.carpetgui.ui.core.Color;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.VerticalAlignment;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class ScreenTabBar {
   public static void build(FlowLayout root, Tab activeTab) {
      FlowLayout tabBar = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fill(7));
      tabBar.surface(Surface.flat(1712986650));
      tabBar.verticalAlignment(VerticalAlignment.CENTER);
      tabBar.gap(2);
      tabBar.padding(Insets.of(2, 0, 4, 4));

      for(Tab tab : ScreenTabBar.Tab.values()) {
         boolean isActive = tab == activeTab;
         LabelComponent label = UIComponents.label(Component.translatable(tab.key));
         label.color(Color.ofArgb(isActive ? -1 : 1722460842));
         FlowLayout btn = UIContainers.horizontalFlow(Sizing.content(), Sizing.fill(100));
         btn.verticalAlignment(VerticalAlignment.CENTER);
         btn.padding(Insets.horizontal(8));
         btn.carpetGUI$cursorStyle(CursorStyle.HAND);
         btn.surface(Surface.flat(isActive ? 1724895183 : 0).and(Surface.outline(1722789807)));
         btn.child(label);
         if (!isActive) {
            btn.carpetGUI$mouseDown().subscribe((MouseDown)(mouseButtonEvent, b) -> {
               onTabClick(tab);
               Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               return true;
            });
         }

         tabBar.child(btn);
      }

      root.child(0, tabBar);
   }

   private static void onTabClick(Tab tab) {
      switch (tab.ordinal()) {
         case 0:
            CarpetGUIClientPacketHandler.openRuleEditScreen(true);
            break;
         case 1:
            Minecraft.getInstance().setScreenAndShow(new RuleStackScreen());
            ClientPlayNetworking.send(new RequestRuleStackPayload());
            break;
         case 2:
            Minecraft.getInstance().setScreenAndShow(new RuleGroupScreen());
      }

   }

   public static enum Tab {
      RULES(0, "gui.tab.rules"),
      STACK(1, "gui.tab.stack"),
      GROUPS(2, "gui.tab.groups");

      public final int index;
      public final String key;

      private Tab(int index, String key) {
         this.index = index;
         this.key = key;
      }

      private static Tab[] $values() {
         return new Tab[]{RULES, STACK, GROUPS};
      }
   }
}
