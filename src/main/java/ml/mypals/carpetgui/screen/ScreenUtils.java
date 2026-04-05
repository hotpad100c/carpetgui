package ml.mypals.carpetgui.screen;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import ml.mypals.carpetgui.network.RuleData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
//?if>1.20.1{
import net.minecraft.network.chat.contents.PlainTextContents;
//?}else{
/*import net.minecraft.network.chat.ComponentContents;
*///?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.Map;
import java.util.function.Consumer;

import static ml.mypals.carpetgui.CarpetGUI.MOD_ID;

public class ScreenUtils {
    public static final ResourceLocation RESET = rl("ui/reset.png");
    public static final ResourceLocation NO = rl("ui/x.png");
    public static final ResourceLocation LOCK_ON = rl("ui/lock.png");
    public static final ResourceLocation LOCK_OFF = rl("ui/unlock.png");
    public static final ResourceLocation LOVE_ON = rl("ui/loved.png");
    public static final ResourceLocation LOVE_OFF = rl("ui/love.png");
    public static final ResourceLocation TRUE_TEX = rl("ui/true_t.png");
    public static final ResourceLocation FALSE_TEX = rl("ui/false_t.png");

    public static FlowLayout buildSpriteToggle(ResourceLocation initTex, int w, int h,
                                               java.util.function.Consumer<FlowLayout> onClick) {
        var wrapper = /*? if <1.21.11 {*/Containers/*?} else {*//*UIContainers*//*?}*/.horizontalFlow(Sizing.fixed(w + 2), Sizing.fixed(h));
        wrapper.verticalAlignment(VerticalAlignment.CENTER);
        wrapper.horizontalAlignment(HorizontalAlignment.CENTER);
        wrapper.cursorStyle(CursorStyle.HAND);
        wrapper.child(makeTexture(initTex, w, h));
        //? if <1.21.9 {
        wrapper.mouseDown().subscribe((x, y, btn) -> {
        //?} else {
        /*wrapper.mouseDown().subscribe((mouseButtonEvent, btn) -> {
        *///?}
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            onClick.accept(wrapper);
            return true;
        });
        return wrapper;
    }

    public static Map.Entry<FlowLayout, FlowLayout> makeMasterContainer(int w, int h, FlowLayout root){

        ScaleHelper.Result scaleResult = ScaleHelper.compute(w, h);
        var outline = /*? if <1.21.11 {*/Containers/*?} else {*//*UIContainers*//*?}*/.verticalFlow(Sizing.fixed(scaleResult.width), Sizing.fixed(scaleResult.height));

        outline.surface(Surface.outline(0x66AFAFAF));
        var content =/*? if <1.21.11 {*/Containers/*?} else {*//*UIContainers*//*?}*/.horizontalFlow(Sizing.fill(100), Sizing.fill(95));
        content.surface(Surface.flat(0x77000000));
        outline.padding(Insets.of(1));

        root.padding(Insets.of(ScaleHelper.PAD_TOP, ScaleHelper.PAD_BOTTOM, ScaleHelper.PAD_LEFT, ScaleHelper.PAD_RIGHT));
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.verticalAlignment(VerticalAlignment.CENTER);
        root.surface(Surface.blur(4,4));
        outline.child(content);

        return Map.entry(outline, content);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void swapTexture(FlowLayout wrapper, ResourceLocation newTex, int w, int h) {
        wrapper.clearChildren();
        wrapper.child(makeTexture(newTex, w, h));
    }

    public static TextureComponent makeTexture(ResourceLocation tex, int w, int h) {
        var t = /*? if <1.21.11 {*/Components/*?} else {*//*UIComponents*//*?}*/.texture(tex, 0, 0, w, h, w, h);
        t.sizing(Sizing.fixed(w), Sizing.fixed(h));
        return t;
    }

    public static DialogResult createSaveGroupDialog(Consumer<String> saveAction, Consumer<String> cancelAction) {
        FlowLayout saveDialog = /*? if <1.21.11 {*/Containers/*?} else {*//*UIContainers*//*?}*/.verticalFlow(Sizing.fixed(200), Sizing.content());
        saveDialog.surface(Surface.VANILLA_TRANSLUCENT.and(Surface.outline(0x77CFCFCF))).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        saveDialog.child(
                /*? if <1.21.11 {*/Components/*?} else {*//*UIComponents*//*?}*/.label(Component.translatable("gui.rulegroups.save_group"))
                        .color(Color.WHITE)
                        .shadow(true)
        );

        TextBoxComponent nameBox = /*? if <1.21.11 {*/Components/*?} else {*//*UIComponents*//*?}*/.textBox(Sizing.fill(70));
        nameBox.setMaxLength(64);
        nameBox.text("modified_" + System.currentTimeMillis());
        saveDialog.child(nameBox);

        FlowLayout buttons = /*? if <1.21.11 {*/Containers/*?} else {*//*UIContainers*//*?}*/.horizontalFlow(Sizing.fill(80), Sizing.fixed(26));
        buttons.gap(8).horizontalAlignment(HorizontalAlignment.CENTER);

        ButtonComponent cancel = /*? if <1.21.11 {*/Components/*?} else {*//*UIComponents*//*?}*/.button(Component.translatable("gui.rulegroups.cancel"),
                b -> cancelAction.accept(""));
        cancel.sizing(Sizing.fill(40), Sizing.fixed(22));

        ButtonComponent saveBtn = /*? if <1.21.11 {*/Components/*?} else {*//*UIComponents*//*?}*/.button(Component.translatable("gui.rulegroups.save"),
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
        saveDialog.padding(Insets.of(5));
        //? if <1.21.5 {
        saveDialog.zIndex(500);
        //?}
        OverlayContainer<FlowLayout> dialogOverlay = /*? if <1.21.11 {*/Containers/*?} else {*//*UIContainers*//*?}*/.overlay(saveDialog);
        dialogOverlay.surface(Surface.flat(0));
        dialogOverlay.closeOnClick(true);
        return new DialogResult(dialogOverlay, saveDialog);
    }


    public static void showSaveGroupDialog(FlowLayout rootComponent, OverlayContainer<FlowLayout> dialogOverlay) {

        if (dialogOverlay.parent() != null) return;

        rootComponent.child(rootComponent.children().size(), dialogOverlay);
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
    public static FlowLayout btn(Component label,Sizing ws,Sizing hs, Runnable action) {
        var b = /*? if <1.21.11 {*/Containers/*?} else {*//*UIContainers*//*?}*/.horizontalFlow(ws, hs);
        b.surface(Surface.flat(0x35FFFFFF).and(Surface.outline(0x55FFFFFF)));
        b.verticalAlignment(VerticalAlignment.CENTER);
        b.horizontalAlignment(HorizontalAlignment.CENTER);
        b.cursorStyle(CursorStyle.HAND);
        b.padding(Insets.of(0,0,2,2));
        b.child(/*? if <1.21.11 {*/Components/*?} else {*//*UIComponents*//*?}*/.label(label).color(Color.WHITE));
        b.mouseEnter().subscribe(()-> b.surface(Surface.flat(0x35FFFFFF).and(Surface.outline(0xFFFFFFFF))));
        b.mouseLeave().subscribe(()-> b.surface(Surface.flat(0x35FFFFFF).and(Surface.outline(0x55FFFFFF))));
        //? if <1.21.9 {
        b.mouseDown().subscribe((x, y, btn) -> {
        //?} else {
        /*b.mouseDown().subscribe((mouseButtonEvent, btn) -> {
        *///?}
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            action.run();
            return true;
        });
        return b;
    }
    public static final class ScaleHelper {

        public static final int DESIGN_WIDTH = 420;
        public static final int DESIGN_HEIGHT = 200;

        public static final int PAD_TOP = 2;
        public static final int PAD_RIGHT = 2;
        public static final int PAD_BOTTOM = 2;
        public static final int PAD_LEFT = 2;

        public static final double SCALE_MIN = 0.7;
        public static final double SCALE_MAX = 1.2;

        public record Result(double scale, int width, int height) {}
        public static Result compute(int screenW, int screenH) {
            return compute(screenW, screenH, DESIGN_WIDTH, DESIGN_HEIGHT);
        }

        public static Result compute(int screenW, int screenH, int designW, int designH) {
            int availableW = screenW - PAD_LEFT - PAD_RIGHT;
            int availableH = screenH - PAD_TOP  - PAD_BOTTOM;
            double scale = Math.min((double) availableW / designW, (double) availableH / designH);
            scale = Math.max(SCALE_MIN, Math.min(scale, SCALE_MAX));
            return new Result(scale, (int) (designW * scale), (int) (designH * scale));
        }

        private ScaleHelper() {}
    }
    public static Component buildTooltip(RuleData ruleData, String query) {
        //? if >1.20.1 {
        var tip = MutableComponent.create(PlainTextContents.EMPTY);
        //?} else {
        /*var tip = MutableComponent.create(ComponentContents.EMPTY);
         *///?}
        tip
                .append(highlight(ruleData.localName.isEmpty() ? ruleData.name : ruleData.localName, query)
                        .copy().withStyle(ChatFormatting.WHITE)).append("\n")
                .append(highlight(ruleData.localDescription.isEmpty() ? ruleData.description : ruleData.localDescription, query)
                        .copy().withStyle(ChatFormatting.GRAY)).append("\n")
                .append(Component.translatable("gui.screen.tooltip.defaultValue").withStyle(ChatFormatting.DARK_GREEN))
                .append(": " + ruleData.defaultValue).append("\n")
                .append(Component.translatable("gui.screen.tooltip.currentValue").withStyle(ChatFormatting.DARK_GREEN))
                .append(": " + ruleData.value).append("\n")
                .append(Component.translatable("gui.screen.tooltip.suggestions").withStyle(ChatFormatting.BLUE)).append(":");
        tip.append(" [");
        for (int i = 0; i < ruleData.suggestions.size(); i++) {
            tip.append(Component.literal(
                    ruleData.suggestions.get(i) +
                            (i + 1 < ruleData.suggestions.size() ? ", " : "")
            ).withStyle(ChatFormatting.GRAY));
        }
        tip.append("]");
        return tip;
    }
    public static Component highlight(String text, String query) {
        if (query == null || query.isEmpty()) {
            return Component.nullToEmpty(text);
        }

        var result = Component.empty().copy();
        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();
        int start = 0;

        while (true) {
            int idx = lowerText.indexOf(lowerQuery, start);
            if (idx == -1) {
                if (start < text.length()) {
                    result.append(Component.nullToEmpty(text.substring(start)));
                }
                break;
            }
            if (idx > start) {
                result.append(Component.nullToEmpty(text.substring(start, idx)));
            }
            result.append(
                    Component.nullToEmpty(text.substring(idx, idx + query.length()))
                            .copy()
                            .withStyle(style -> style
                                    .withColor(net.minecraft.ChatFormatting.YELLOW)
                                    .withBold(true)
                            )
            );

            start = idx + query.length();
        }

        return result;
    }
}
