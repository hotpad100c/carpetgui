package ml.mypals.carpetgui.screen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.client.CarpetGUIClientPacketHandler;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
import ml.mypals.carpetgui.screen.ruleStack.RuleStackScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;


public class ScreenTabBar {

    public enum Tab {
        RULES(0,   "gui.tab.rules"),
        STACK(1,   "gui.tab.stack"),
        GROUPS(2,  "gui.tab.groups");

        public final int index;
        public final String key;
        Tab(int index, String key) { this.index = index; this.key = key; }
    }

    public static void build(FlowLayout root, Tab activeTab) {
        FlowLayout tabBar = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(
                Sizing.fill(100), Sizing.fill(7));
        tabBar.surface(Surface.flat(0x661A1A1A));
        tabBar.verticalAlignment(VerticalAlignment.CENTER);
        tabBar.gap(2);
        tabBar.padding(Insets.of(2,0,4,4));

        for (Tab tab : Tab.values()) {
            boolean isActive = tab == activeTab;

            var label = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(Component.translatable(tab.key));
            label.color(Color.ofArgb(isActive ? 0xFFFFFFFF : 0x66AAAAAA));

            FlowLayout btn = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(
                    Sizing.content(), Sizing.fill(100));
            btn.verticalAlignment(VerticalAlignment.CENTER);
            btn.padding(Insets.horizontal(8));
            btn.cursorStyle(CursorStyle.HAND);
            btn.surface(Surface.flat(isActive ? 0x66CFCFCF : 0x00000000).and(Surface.outline(0x66AFAFAF)));
            btn.child(label);

            if (!isActive) {
                //? if <1.21.9 {
                /*btn.mouseDown().subscribe((x, y, b) -> {
                 *///?} else {
                btn.mouseDown().subscribe((mouseButtonEvent, b) -> {
                //?}
                    onTabClick(tab);
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
                    return true;
                });
            }

            tabBar.child(btn);
        }

        root.child(0, tabBar);
    }

    private static void onTabClick(Tab tab) {
        switch (tab) {
            case RULES  -> CarpetGUIClientPacketHandler.openRuleEditScreen(true);
            case STACK  -> {
                Minecraft.getInstance().setScreen(new RuleStackScreen());
                ClientPlayNetworking.send(new RequestRuleStackPayload());
            }
            case GROUPS -> Minecraft.getInstance().setScreen(new RuleGroupScreen());
        }
    }
}
