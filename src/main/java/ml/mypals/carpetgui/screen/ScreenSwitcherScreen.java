package ml.mypals.carpetgui.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
import ml.mypals.carpetgui.screen.ruleStack.RuleStackScreen;
import ml.mypals.carpetgui.screen.rulesEditScreen.RulesEditScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
//? if >1.19.4 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
 *///?}
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScreenSwitcherScreen {
    /** 统一入口，替代原先 new ScreenSwitcherScreen() 的所有调用处 */
    public static void open() {
        CarpetGUIClient.openRuleEditScreen(true);
    }
}