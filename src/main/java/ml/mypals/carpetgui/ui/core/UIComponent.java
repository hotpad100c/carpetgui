package ml.mypals.carpetgui.ui.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import ml.mypals.carpetgui.ui.util.EventStream;
import ml.mypals.carpetgui.ui.util.FocusHandler;
import ml.mypals.carpetgui.ui.util.Observable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?} else {
/*import ml.mypals.carpetgui.compat.input.CharacterEvent;
import ml.mypals.carpetgui.compat.input.KeyEvent;
import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UIComponent extends PositionedRectangle {
   void carpetGUI$draw(OwoUIGraphics var1, int var2, int var3, float var4, float var5);

   default void drawTooltip(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
      if (this.carpetGUI$shouldDrawTooltip((double)mouseX, (double)mouseY)) {
         context.drawTooltip(Minecraft.getInstance().font, mouseX, mouseY, this.carpetGUI$tooltip());
      }
   }

   default void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
      context.drawRectOutline(this.carpetGUI$x(), this.carpetGUI$y(), this.carpetGUI$width(), this.carpetGUI$height(), -1);
   }

   @Contract(
      pure = true
   )
   @Nullable ParentUIComponent carpetGUI$parent();

   @Contract(
      pure = true
   )
   @Nullable FocusHandler carpetGUI$focusHandler();

   UIComponent carpetGUI$positioning(Positioning var1);

   @Contract(
      pure = true
   )
   Observable<Positioning> carpetGUI$positioning();

   UIComponent carpetGUI$margins(Insets var1);

   @Contract(
      pure = true
   )
   Observable<Insets> carpetGUI$margins();

   default UIComponent sizing(Sizing horizontalSizing, Sizing verticalSizing) {
      this.carpetGUI$horizontalSizing(horizontalSizing);
      this.carpetGUI$verticalSizing(verticalSizing);
      return this;
   }

   default UIComponent sizing(Sizing sizing) {
      this.sizing(sizing, sizing);
      return this;
   }

   UIComponent carpetGUI$horizontalSizing(Sizing var1);

   @Contract(
      pure = true
   )
   Observable<Sizing> carpetGUI$horizontalSizing();

   UIComponent carpetGUI$verticalSizing(Sizing var1);

   @Contract(
      pure = true
   )
   Observable<Sizing> carpetGUI$verticalSizing();

   UIComponent carpetGUI$id(@Nullable String var1);

   @Nullable String carpetGUI$id();

   UIComponent carpetGUI$tooltip(@Nullable List<ClientTooltipComponent> var1);

   default UIComponent carpetGUI$tooltip(@NotNull Collection<Component> tooltip) {
      ArrayList<ClientTooltipComponent> components = new ArrayList();

      for(Component line : tooltip) {
         components.add(ClientTooltipComponent.create(line.getVisualOrderText()));
      }

      this.carpetGUI$tooltip(components);
      return this;
   }

   default UIComponent carpetGUI$tooltip(@NotNull Component tooltip) {
      ArrayList<ClientTooltipComponent> components = new ArrayList();

      for(FormattedCharSequence line : Minecraft.getInstance().font.split(tooltip, Integer.MAX_VALUE)) {
         components.add(ClientTooltipComponent.create(line));
      }

      this.carpetGUI$tooltip(components);
      return this;
   }

   default UIComponent tooltip(@NotNull Collection<Component> tooltip) {
      return this.carpetGUI$tooltip(tooltip);
   }

   default UIComponent tooltip(@NotNull Component tooltip) {
      return this.carpetGUI$tooltip(tooltip);
   }

   @Contract(
      pure = true
   )
   @Nullable List<ClientTooltipComponent> carpetGUI$tooltip();

   default boolean carpetGUI$shouldDrawTooltip(double mouseX, double mouseY) {
      return this.carpetGUI$tooltip() != null && !this.carpetGUI$tooltip().isEmpty() && this.carpetGUI$isInBoundingBox(mouseX, mouseY);
   }

   void carpetGUI$inflate(Size var1);

   void carpetGUI$mount(ParentUIComponent var1, int var2, int var3);

   void carpetGUI$dismount(DismountReason var1);

   <C extends UIComponent> C carpetGUI$configure(Consumer<C> var1);

   @Contract(
      pure = true
   )
   default boolean carpetGUI$hasParent() {
      return this.carpetGUI$parent() != null;
   }

   default ParentUIComponent carpetGUI$root() {
      ParentUIComponent root = this.carpetGUI$parent();
      if (root == null) {
         return null;
      } else {
         while(root.carpetGUI$hasParent()) {
            root = root.carpetGUI$parent();
         }

         return root;
      }
   }

   default void carpetGUI$remove() {
      if (this.carpetGUI$hasParent()) {
         this.carpetGUI$parent().queue(() -> this.carpetGUI$parent().removeChild(this));
      }
   }

   boolean carpetGUI$onMouseDown(MouseButtonEvent var1, boolean var2);

   EventStream<MouseDown> carpetGUI$mouseDown();

   boolean carpetGUI$onMouseUp(MouseButtonEvent var1);

   EventStream<MouseUp> carpetGUI$mouseUp();

   boolean carpetGUI$onMouseScroll(double var1, double var3, double var5);

   EventStream<MouseScroll> carpetGUI$mouseScroll();

   boolean carpetGUI$onMouseDrag(MouseButtonEvent var1, double var2, double var4);

   EventStream<MouseDrag> carpetGUI$mouseDrag();

   boolean carpetGUI$onKeyPress(KeyEvent var1);

   EventStream<KeyPress> carpetGUI$keyPress();

   boolean carpetGUI$onCharTyped(CharacterEvent var1);

   EventStream<CharTyped> carpetGUI$charTyped();

   default boolean carpetGUI$canFocus(FocusSource source) {
      return false;
   }

   void carpetGUI$onFocusGained(FocusSource var1);

   EventStream<FocusGained> carpetGUI$focusGained();

   void carpetGUI$onFocusLost();

   EventStream<FocusLost> carpetGUI$focusLost();

   EventStream<MouseEnter> carpetGUI$mouseEnter();

   EventStream<MouseLeave> carpetGUI$mouseLeave();

   CursorStyle carpetGUI$cursorStyle();

   UIComponent carpetGUI$cursorStyle(CursorStyle var1);

   default void carpetGUI$update(float delta, int mouseX, int mouseY) {
   }

   default boolean carpetGUI$isInBoundingBox(double x, double y) {
      return PositionedRectangle.super.carpetGUI$isInBoundingBox(x, y);
   }

   default Size carpetGUI$fullSize() {
      Insets margins = (Insets)this.carpetGUI$margins().get();
      return Size.of(this.carpetGUI$width() + margins.horizontal(), this.carpetGUI$height() + margins.vertical());
   }

   @Contract(
      pure = true
   )
   int carpetGUI$width();

   @Contract(
      pure = true
   )
   int carpetGUI$height();

   @Contract(
      pure = true
   )
   int carpetGUI$x();

   default int carpetGUI$baseX() {
      return this.carpetGUI$x();
   }

   void carpetGUI$updateX(int var1);

   @Contract(
      pure = true
   )
   int carpetGUI$y();

   default int carpetGUI$baseY() {
      return this.carpetGUI$y();
   }

   void carpetGUI$updateY(int var1);

   default void carpetGUI$moveTo(int x, int y) {
      this.carpetGUI$updateX(x);
      this.carpetGUI$updateY(y);
   }

   default MutableComponent carpetGUI$inspectorDescriptor() {
      Insets margins = (Insets)this.carpetGUI$margins().get();
      int var10000 = this.carpetGUI$x();
      MutableComponent var2 = Component.literal(var10000 + "," + this.carpetGUI$y() + " (" + this.carpetGUI$width() + "," + this.carpetGUI$height() + ")");
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
