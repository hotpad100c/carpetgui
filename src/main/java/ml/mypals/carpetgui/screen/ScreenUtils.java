package ml.mypals.carpetgui.screen;

import java.util.Map;
import java.util.function.Consumer;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.ui.component.ButtonComponent;
import ml.mypals.carpetgui.ui.component.TextBoxComponent;
import ml.mypals.carpetgui.ui.component.TextureComponent;
import ml.mypals.carpetgui.ui.component.UIComponents;
import ml.mypals.carpetgui.ui.container.FlowLayout;
import ml.mypals.carpetgui.ui.container.OverlayContainer;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public class ScreenUtils {
   public static final Identifier RESET = rl("ui/reset.png");
   public static final Identifier NO = rl("ui/x.png");
   public static final Identifier LOCK_ON = rl("ui/lock.png");
   public static final Identifier LOCK_OFF = rl("ui/unlock.png");
   public static final Identifier LOVE_ON = rl("ui/loved.png");
   public static final Identifier LOVE_OFF = rl("ui/love.png");
   public static final Identifier TRUE_TEX = rl("ui/true_t.png");
   public static final Identifier FALSE_TEX = rl("ui/false_t.png");

   public static FlowLayout buildSpriteToggle(Identifier initTex, int w, int h, Consumer<FlowLayout> onClick) {
      FlowLayout wrapper = UIContainers.horizontalFlow(Sizing.fixed(w + 2), Sizing.fixed(h));
      wrapper.verticalAlignment(VerticalAlignment.CENTER);
      wrapper.horizontalAlignment(HorizontalAlignment.CENTER);
      wrapper.cursorStyle(CursorStyle.HAND);
      wrapper.child(makeTexture(initTex, w, h));
      wrapper.mouseDown().subscribe((MouseDown)(mouseButtonEvent, btn) -> {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         onClick.accept(wrapper);
         return true;
      });
      return wrapper;
   }

   public static Map.Entry<FlowLayout, FlowLayout> makeMasterContainer(int w, int h, FlowLayout root) {
      ScaleHelper.Result scaleResult = ScreenUtils.ScaleHelper.compute(w, h);
      FlowLayout outline = UIContainers.verticalFlow(Sizing.fixed(scaleResult.width), Sizing.fixed(scaleResult.height));
      outline.surface(Surface.outline(1722789807));
      FlowLayout content = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fill(95));
      content.surface(Surface.flat(1996488704));
      outline.padding(Insets.of(1));
      root.padding(Insets.of(2, 2, 2, 2));
      root.horizontalAlignment(HorizontalAlignment.CENTER);
      root.verticalAlignment(VerticalAlignment.CENTER);
      root.surface(Surface.blur(4.0F, 4.0F));
      outline.child(content);
      return Map.entry(outline, content);
   }

   public static Identifier rl(String path) {
      return Identifier.fromNamespaceAndPath("carpetgui", path);
   }

   public static void swapTexture(FlowLayout wrapper, Identifier newTex, int w, int h) {
      wrapper.clearChildren();
      wrapper.child(makeTexture(newTex, w, h));
   }

   public static TextureComponent makeTexture(Identifier tex, int w, int h) {
      TextureComponent t = UIComponents.texture(tex, 0, 0, w, h, w, h);
      t.sizing(Sizing.fixed(w), Sizing.fixed(h));
      return t;
   }

   public static DialogResult createSaveGroupDialog(Consumer<String> saveAction, Consumer<String> cancelAction) {
      FlowLayout saveDialog = UIContainers.verticalFlow(Sizing.fixed(200), Sizing.content());
      saveDialog.surface(Surface.VANILLA_TRANSLUCENT.and(Surface.outline(2010107855))).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      saveDialog.child(UIComponents.label(Component.translatable("gui.rulegroups.save_group")).color(Color.WHITE).shadow(true));
      TextBoxComponent nameBox = UIComponents.textBox(Sizing.fill(70));
      nameBox.setMaxLength(64);
      nameBox.text("modified_" + System.currentTimeMillis());
      saveDialog.child(nameBox);
      FlowLayout buttons = UIContainers.horizontalFlow(Sizing.fill(80), Sizing.fixed(26));
      buttons.gap(8).horizontalAlignment(HorizontalAlignment.CENTER);
      ButtonComponent cancel = UIComponents.button(Component.translatable("gui.rulegroups.cancel"), (b) -> cancelAction.accept(""));
      cancel.sizing(Sizing.fill(40), Sizing.fixed(22));
      ButtonComponent saveBtn = UIComponents.button(Component.translatable("gui.rulegroups.save"), (b) -> {
         String groupName = nameBox.getValue().trim();
         if (groupName.isEmpty()) {
            groupName = "modified_" + System.currentTimeMillis();
         }

         saveAction.accept(groupName);
         cancelAction.accept(groupName);
      });
      saveBtn.sizing(Sizing.fill(40), Sizing.fixed(22));
      buttons.child(cancel);
      buttons.child(saveBtn);
      saveDialog.child(buttons);
      saveDialog.padding(Insets.of(5));
      OverlayContainer<FlowLayout> dialogOverlay = UIContainers.<FlowLayout>overlay(saveDialog);
      dialogOverlay.surface(Surface.flat(0));
      dialogOverlay.closeOnClick(true);
      return new DialogResult(dialogOverlay, saveDialog);
   }

   public static void showSaveGroupDialog(FlowLayout rootComponent, OverlayContainer<FlowLayout> dialogOverlay) {
      if (dialogOverlay.parent() == null) {
         rootComponent.child(rootComponent.children().size(), dialogOverlay);
      }
   }

   public static void hideSaveDialog(FlowLayout rootComponent, OverlayContainer<FlowLayout> dialogOverlay) {
      if (dialogOverlay != null && dialogOverlay.parent() != null) {
         rootComponent.removeChild(dialogOverlay);
      }

   }

   public static String truncateWithEllipsis(String text, Font font, int maxWidth) {
      if (font.width(text) <= maxWidth) {
         return text;
      } else {
         String ellipsis = "...";

         for(int i = text.length() - 1; i > 0; --i) {
            String var10000 = text.substring(0, i);
            String candidate = var10000 + ellipsis;
            if (font.width(candidate) <= maxWidth) {
               return candidate;
            }
         }

         char var6 = text.charAt(0);
         return var6 + ellipsis;
      }
   }

   public static FlowLayout btn(Component label, Sizing ws, Sizing hs, Runnable action) {
      FlowLayout b = UIContainers.horizontalFlow(ws, hs);
      b.surface(Surface.flat(905969663).and(Surface.outline(1442840575)));
      b.verticalAlignment(VerticalAlignment.CENTER);
      b.horizontalAlignment(HorizontalAlignment.CENTER);
      b.cursorStyle(CursorStyle.HAND);
      b.padding(Insets.of(0, 0, 2, 2));
      b.child(UIComponents.label(label).color(Color.WHITE));
      b.mouseEnter().subscribe((MouseEnter)() -> b.surface(Surface.flat(905969663).and(Surface.outline(-1))));
      b.mouseLeave().subscribe((MouseLeave)() -> b.surface(Surface.flat(905969663).and(Surface.outline(1442840575))));
      b.mouseDown().subscribe((MouseDown)(mouseButtonEvent, btn) -> {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         action.run();
         return true;
      });
      return b;
   }

   public static Component buildTooltip(RuleData ruleData, String query) {
      MutableComponent tip = MutableComponent.create(PlainTextContents.EMPTY);
      tip.append(highlight(ruleData.localName.isEmpty() ? ruleData.name : ruleData.localName, query).copy().withStyle(ChatFormatting.WHITE)).append("\n").append(highlight(ruleData.localDescription.isEmpty() ? ruleData.description : ruleData.localDescription, query).copy().withStyle(ChatFormatting.GRAY)).append("\n").append(Component.translatable("gui.screen.tooltip.defaultValue").withStyle(ChatFormatting.DARK_GREEN)).append(": " + ruleData.defaultValue).append("\n").append(Component.translatable("gui.screen.tooltip.currentValue").withStyle(ChatFormatting.DARK_GREEN)).append(": " + ruleData.value).append("\n").append(Component.translatable("gui.screen.tooltip.suggestions").withStyle(ChatFormatting.BLUE)).append(":");
      tip.append(" [");

      for(int i = 0; i < ruleData.suggestions.size(); ++i) {
         String var10001 = (String)ruleData.suggestions.get(i);
         tip.append(Component.literal(var10001 + (i + 1 < ruleData.suggestions.size() ? ", " : "")).withStyle(ChatFormatting.GRAY));
      }

      tip.append("]");
      return tip;
   }

   public static Component highlight(String text, String query) {
      if (query != null && !query.isEmpty()) {
         MutableComponent result = Component.empty().copy();
         String lowerText = text.toLowerCase();
         String lowerQuery = query.toLowerCase();
         int start = 0;

         while(true) {
            int idx = lowerText.indexOf(lowerQuery, start);
            if (idx == -1) {
               if (start < text.length()) {
                  result.append(Component.nullToEmpty(text.substring(start)));
               }

               return result;
            }

            if (idx > start) {
               result.append(Component.nullToEmpty(text.substring(start, idx)));
            }

            result.append(Component.nullToEmpty(text.substring(idx, idx + query.length())).copy().withStyle((style) -> style.withColor(ChatFormatting.YELLOW).withBold(true)));
            start = idx + query.length();
         }
      } else {
         return Component.nullToEmpty(text);
      }
   }

   public static record DialogResult(OverlayContainer<FlowLayout> overlay, FlowLayout dialog) {
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

      public static Result compute(int screenW, int screenH) {
         return compute(screenW, screenH, 420, 200);
      }

      public static Result compute(int screenW, int screenH, int designW, int designH) {
         int availableW = screenW - 2 - 2;
         int availableH = screenH - 2 - 2;
         double scale = Math.min((double)availableW / (double)designW, (double)availableH / (double)designH);
         scale = Math.max(0.7, Math.min(scale, 1.2));
         return new Result(scale, (int)((double)designW * scale), (int)((double)designH * scale));
      }

      private ScaleHelper() {
      }

      public static record Result(double scale, int width, int height) {
      }
   }
}
