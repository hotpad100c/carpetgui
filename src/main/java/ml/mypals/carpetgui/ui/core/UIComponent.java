package ml.mypals.carpetgui.ui.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import ml.mypals.carpetgui.ui.util.EventSource;
import ml.mypals.carpetgui.ui.util.FocusHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UIComponent extends PositionedRectangle {
   void draw(OwoUIGraphics var1, int var2, int var3, float var4, float var5);

   default void drawTooltip(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
      if (this.shouldDrawTooltip((double)mouseX, (double)mouseY)) {
         context.drawTooltip(Minecraft.getInstance().font, mouseX, mouseY, this.tooltip());
      }
   }

   default void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
      context.drawRectOutline(this.x(), this.y(), this.width(), this.height(), -1);
   }

   @Contract(
      pure = true
   )
   @Nullable ParentUIComponent parent();

   @Contract(
      pure = true
   )
   @Nullable FocusHandler focusHandler();

   UIComponent positioning(Positioning var1);

   @Contract(
      pure = true
   )
   AnimatableProperty<Positioning> positioning();

   UIComponent margins(Insets var1);

   @Contract(
      pure = true
   )
   AnimatableProperty<Insets> margins();

   default UIComponent sizing(Sizing horizontalSizing, Sizing verticalSizing) {
      this.horizontalSizing(horizontalSizing);
      this.verticalSizing(verticalSizing);
      return this;
   }

   default UIComponent sizing(Sizing sizing) {
      this.sizing(sizing, sizing);
      return this;
   }

   UIComponent horizontalSizing(Sizing var1);

   @Contract(
      pure = true
   )
   AnimatableProperty<Sizing> horizontalSizing();

   UIComponent verticalSizing(Sizing var1);

   @Contract(
      pure = true
   )
   AnimatableProperty<Sizing> verticalSizing();

   UIComponent id(@Nullable String var1);

   @Nullable String id();

   UIComponent tooltip(@Nullable List<ClientTooltipComponent> var1);

   default UIComponent tooltip(@NotNull Collection<Component> tooltip) {
      ArrayList<ClientTooltipComponent> components = new ArrayList();

      for(Component line : tooltip) {
         components.add(ClientTooltipComponent.create(line.getVisualOrderText()));
      }

      this.tooltip(components);
      return this;
   }

   default UIComponent tooltip(@NotNull Component tooltip) {
      ArrayList<ClientTooltipComponent> components = new ArrayList();

      for(FormattedCharSequence line : Minecraft.getInstance().font.split(tooltip, Integer.MAX_VALUE)) {
         components.add(ClientTooltipComponent.create(line));
      }

      this.tooltip(components);
      return this;
   }

   @Contract(
      pure = true
   )
   @Nullable List<ClientTooltipComponent> tooltip();

   default boolean shouldDrawTooltip(double mouseX, double mouseY) {
      return this.tooltip() != null && !this.tooltip().isEmpty() && this.isInBoundingBox(mouseX, mouseY);
   }

   void inflate(Size var1);

   void mount(ParentUIComponent var1, int var2, int var3);

   void dismount(DismountReason var1);

   <C extends UIComponent> C configure(Consumer<C> var1);

   @Contract(
      pure = true
   )
   default boolean hasParent() {
      return this.parent() != null;
   }

   default ParentUIComponent root() {
      ParentUIComponent root = this.parent();
      if (root == null) {
         return null;
      } else {
         while(root.hasParent()) {
            root = root.parent();
         }

         return root;
      }
   }

   default void remove() {
      if (this.hasParent()) {
         this.parent().queue(() -> this.parent().removeChild(this));
      }
   }

   boolean onMouseDown(MouseButtonEvent var1, boolean var2);

   EventSource<MouseDown> mouseDown();

   boolean onMouseUp(MouseButtonEvent var1);

   EventSource<MouseUp> mouseUp();

   boolean onMouseScroll(double var1, double var3, double var5);

   EventSource<MouseScroll> mouseScroll();

   boolean onMouseDrag(MouseButtonEvent var1, double var2, double var4);

   EventSource<MouseDrag> mouseDrag();

   boolean onKeyPress(KeyEvent var1);

   EventSource<KeyPress> keyPress();

   boolean onCharTyped(CharacterEvent var1);

   EventSource<CharTyped> charTyped();

   default boolean canFocus(FocusSource source) {
      return false;
   }

   void onFocusGained(FocusSource var1);

   EventSource<FocusGained> focusGained();

   void onFocusLost();

   EventSource<FocusLost> focusLost();

   EventSource<MouseEnter> mouseEnter();

   EventSource<MouseLeave> mouseLeave();

   CursorStyle cursorStyle();

   UIComponent cursorStyle(CursorStyle var1);

   default void update(float delta, int mouseX, int mouseY) {
      this.margins().update(delta);
      this.positioning().update(delta);
      this.horizontalSizing().update(delta);
      this.verticalSizing().update(delta);
   }

   default boolean isInBoundingBox(double x, double y) {
      return PositionedRectangle.super.isInBoundingBox(x, y);
   }

   default Size fullSize() {
      Insets margins = (Insets)this.margins().get();
      return Size.of(this.width() + margins.horizontal(), this.height() + margins.vertical());
   }

   @Contract(
      pure = true
   )
   int width();

   @Contract(
      pure = true
   )
   int height();

   @Contract(
      pure = true
   )
   int x();

   default int baseX() {
      return this.x();
   }

   void updateX(int var1);

   @Contract(
      pure = true
   )
   int y();

   default int baseY() {
      return this.y();
   }

   void updateY(int var1);

   default void moveTo(int x, int y) {
      this.updateX(x);
      this.updateY(y);
   }

   default MutableComponent inspectorDescriptor() {
      Insets margins = (Insets)this.margins().get();
      int var10000 = this.x();
      MutableComponent var2 = Component.literal(var10000 + "," + this.y() + " (" + this.width() + "," + this.height() + ")");
      int var10001 = margins.top();
      return var2.append(Component.literal(" <" + var10001 + "," + margins.bottom() + "," + margins.left() + "," + margins.right() + ">").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
   }

   public static enum FocusSource {
      MOUSE_CLICK,
      KEYBOARD_CYCLE;

      private static FocusSource[] $values() {
         return new FocusSource[]{MOUSE_CLICK, KEYBOARD_CYCLE};
      }
   }

   public static enum DismountReason {
      LAYOUT_INFLATION,
      REMOVED;

      private static DismountReason[] $values() {
         return new DismountReason[]{LAYOUT_INFLATION, REMOVED};
      }
   }
}
