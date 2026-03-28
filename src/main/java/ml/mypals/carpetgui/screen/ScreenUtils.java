package ml.mypals.carpetgui.screen;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.function.Consumer;

import static ml.mypals.carpetgui.CarpetGUI.MOD_ID;

public class ScreenUtils {
    public static final ResourceLocation RESET    = rl("ui/reset.png");
    public static final ResourceLocation NO    = rl("ui/x.png");
    public static final ResourceLocation LOCK_ON    = rl("ui/lock.png");
    public static final ResourceLocation LOCK_OFF   = rl("ui/unlock.png");
    public static final ResourceLocation LOVE_ON    = rl("ui/loved.png");
    public static final ResourceLocation LOVE_OFF   = rl("ui/love.png");
    public static final ResourceLocation TRUE_TEX   = rl("ui/true_t.png");
    public static final ResourceLocation FALSE_TEX  = rl("ui/false_t.png");
    public static FlowLayout buildSpriteToggle(ResourceLocation initTex, int w, int h,
                                         java.util.function.Consumer<FlowLayout> onClick) {
        var wrapper = Containers.horizontalFlow(Sizing.fixed(w + 2), Sizing.fixed(h ));
        wrapper.verticalAlignment(VerticalAlignment.CENTER);
        wrapper.horizontalAlignment(HorizontalAlignment.CENTER);
        wrapper.cursorStyle(CursorStyle.HAND);
        wrapper.child(makeTexture(initTex, w, h));
        wrapper.mouseDown().subscribe((x, y, btn) -> {

            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK,1));
            onClick.accept(wrapper);
            return true;
        });
        return wrapper;
    }
    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
    public static void swapTexture(FlowLayout wrapper, ResourceLocation newTex, int w, int h) {
        wrapper.clearChildren();
        wrapper.child(makeTexture(newTex, w, h));
    }

    public static TextureComponent makeTexture(ResourceLocation tex, int w, int h) {
        var t = Components.texture(tex, 0, 0, w, h, w, h);
        t.sizing(Sizing.fixed(w), Sizing.fixed(h));
        return t;
    }

    public static DialogResult createSaveGroupDialog(Consumer<String> saveAction, Consumer<String> cancelAction) {
        FlowLayout saveDialog = Containers.verticalFlow(Sizing.fixed(200), Sizing.content());
        saveDialog.surface(Surface.VANILLA_TRANSLUCENT)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        saveDialog.child(
                Components.label(Component.translatable(  "carpetgui.rulegroups.save_group"))
                        .color(Color.WHITE)
                        .shadow(true)
        );

        TextBoxComponent nameBox = Components.textBox(Sizing.fill(70));
        nameBox.setMaxLength(64);
        nameBox.text("modified_" + System.currentTimeMillis());
        saveDialog.child(nameBox);

        FlowLayout buttons = Containers.horizontalFlow(Sizing.fill(80), Sizing.fixed(26));
        buttons.gap(8).horizontalAlignment(HorizontalAlignment.CENTER);

        ButtonComponent cancel = Components.button(Component.translatable("carpetgui.rulegroups.cancel"),
                b -> cancelAction.accept(""));
        cancel.sizing(Sizing.fill(40), Sizing.fixed(22));

        ButtonComponent saveBtn = Components.button(Component.translatable("carpetgui.rulegroups.save"),
                b -> {
                    String groupName = nameBox.getValue().trim();
                    if (groupName.isEmpty()) groupName = "modified_" + System.currentTimeMillis();
                    saveAction.accept(groupName);
                    cancelAction.accept(groupName);
                });
        saveBtn.sizing(Sizing.fill(40), Sizing.fixed(22));

        buttons.child(cancel);
        buttons.child(saveBtn);

        saveDialog.child(buttons);

        OverlayContainer<FlowLayout> dialogOverlay = Containers.overlay(saveDialog);
        dialogOverlay.closeOnClick(true);
        dialogOverlay.zIndex(500);
        return new DialogResult(dialogOverlay, saveDialog);
    }


    public static void showSaveGroupDialog(FlowLayout rootComponent, OverlayContainer<FlowLayout> dialogOverlay) {

        if (dialogOverlay.parent() != null) return;

        rootComponent.child(dialogOverlay);
    }


    public static void hideSaveDialog(FlowLayout rootComponent, OverlayContainer<FlowLayout> dialogOverlay) {
        if (dialogOverlay != null && dialogOverlay.parent() != null) {
            rootComponent.removeChild(dialogOverlay);
        }
    }

    public record DialogResult(OverlayContainer<FlowLayout> overlay, FlowLayout dialog) {
    }
    public static String truncateWithEllipsis(String text, Font font, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;

        String ellipsis = "...";
        for (int i = text.length() - 1; i > 0; i--) {
            String candidate = text.substring(0, i) + ellipsis;
            if (font.width(candidate) <= maxWidth) {
                return candidate;
            }
        }
        return text.charAt(0) + ellipsis;
    }
}
