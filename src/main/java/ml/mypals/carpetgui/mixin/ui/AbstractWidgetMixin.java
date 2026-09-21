package ml.mypals.carpetgui.mixin.ui;

import java.util.List;
import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.component.UIComponents;
import ml.mypals.carpetgui.ui.component.VanillaWidgetComponent;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.Positioning;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import ml.mypals.carpetgui.ui.inject.UIComponentStub;
import ml.mypals.carpetgui.ui.util.EventStream;
import ml.mypals.carpetgui.ui.util.FocusHandler;
import ml.mypals.carpetgui.ui.util.Observable;
//? if <26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractWidget.class})
public abstract class AbstractWidgetMixin implements UIComponentStub, GuiEventListener {
   @Shadow
   public boolean active;
   @Shadow
   protected boolean isHovered;
   //? if <1.19 {
   /*@Shadow
   protected abstract void setFocused(boolean var1);
   *///?}
   @Unique
   protected VanillaWidgetComponent wrapper = null;

   public void carpetGUI$inflate(Size space) {
      this.carpetGUI$getWrapper().carpetGUI$inflate(space);
   }

   public void carpetGUI$mount(ParentUIComponent parent, int x, int y) {
      this.carpetGUI$getWrapper().carpetGUI$mount(parent, x, y);
   }

   public void carpetGUI$dismount(UIComponent.DismountReason reason) {
      this.carpetGUI$getWrapper().carpetGUI$dismount(reason);
   }

   public @Nullable ParentUIComponent carpetGUI$parent() {
      return this.carpetGUI$getWrapper().carpetGUI$parent();
   }

   public @Nullable FocusHandler carpetGUI$focusHandler() {
      return this.carpetGUI$getWrapper().carpetGUI$focusHandler();
   }

   public UIComponent carpetGUI$positioning(Positioning positioning) {
      this.carpetGUI$getWrapper().carpetGUI$positioning(positioning);
      return this;
   }

   public Observable<Positioning> carpetGUI$positioning() {
      return this.carpetGUI$getWrapper().carpetGUI$positioning();
   }

   public UIComponent carpetGUI$margins(Insets margins) {
      this.carpetGUI$getWrapper().carpetGUI$margins(margins);
      return this;
   }

   public Observable<Insets> carpetGUI$margins() {
      return this.carpetGUI$getWrapper().carpetGUI$margins();
   }

   public UIComponent carpetGUI$horizontalSizing(Sizing horizontalSizing) {
      this.carpetGUI$getWrapper().carpetGUI$horizontalSizing(horizontalSizing);
      return this;
   }

   public UIComponent carpetGUI$verticalSizing(Sizing verticalSizing) {
      this.carpetGUI$getWrapper().carpetGUI$verticalSizing(verticalSizing);
      return this;
   }

   public Observable<Sizing> carpetGUI$horizontalSizing() {
      return this.carpetGUI$getWrapper().carpetGUI$horizontalSizing();
   }

   public Observable<Sizing> carpetGUI$verticalSizing() {
      return this.carpetGUI$getWrapper().carpetGUI$verticalSizing();
   }

   public EventStream<MouseDown> carpetGUI$mouseDown() {
      return this.carpetGUI$getWrapper().carpetGUI$mouseDown();
   }

   public int carpetGUI$x() {
      return this.carpetGUI$getWrapper().carpetGUI$x();
   }

   public int carpetGUI$y() {
      return this.carpetGUI$getWrapper().carpetGUI$y();
   }

   public int carpetGUI$width() {
      return this.carpetGUI$getWrapper().carpetGUI$width();
   }

   public int carpetGUI$height() {
      return this.carpetGUI$getWrapper().carpetGUI$height();
   }

   public void carpetGUI$draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      this.carpetGUI$getWrapper().carpetGUI$draw(graphics, mouseX, mouseY, partialTicks, delta);
   }

   public boolean carpetGUI$shouldDrawTooltip(double mouseX, double mouseY) {
      return this.carpetGUI$getWrapper().carpetGUI$shouldDrawTooltip(mouseX, mouseY);
   }

   public void carpetGUI$update(float delta, int mouseX, int mouseY) {
      this.carpetGUI$getWrapper().carpetGUI$update(delta, mouseX, mouseY);
      this.carpetGUI$cursorStyle(this.active ? this.carpetGUI$preferredCursorStyle() : CursorStyle.POINTER);
   }

   public boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
      return this.carpetGUI$getWrapper().carpetGUI$onMouseDown(click, doubled);
   }

   public boolean carpetGUI$onMouseUp(MouseButtonEvent click) {
      return this.carpetGUI$getWrapper().carpetGUI$onMouseUp(click);
   }

   public EventStream<MouseUp> carpetGUI$mouseUp() {
      return this.carpetGUI$getWrapper().carpetGUI$mouseUp();
   }

   public EventStream<MouseScroll> carpetGUI$mouseScroll() {
      return this.carpetGUI$getWrapper().carpetGUI$mouseScroll();
   }

   public EventStream<MouseDrag> carpetGUI$mouseDrag() {
      return this.carpetGUI$getWrapper().carpetGUI$mouseDrag();
   }

   public EventStream<KeyPress> carpetGUI$keyPress() {
      return this.carpetGUI$getWrapper().carpetGUI$keyPress();
   }

   public EventStream<CharTyped> carpetGUI$charTyped() {
      return this.carpetGUI$getWrapper().carpetGUI$charTyped();
   }

   public EventStream<FocusGained> carpetGUI$focusGained() {
      return this.carpetGUI$getWrapper().carpetGUI$focusGained();
   }

   public EventStream<FocusLost> carpetGUI$focusLost() {
      return this.carpetGUI$getWrapper().carpetGUI$focusLost();
   }

   public EventStream<MouseEnter> carpetGUI$mouseEnter() {
      return this.carpetGUI$getWrapper().carpetGUI$mouseEnter();
   }

   public EventStream<MouseLeave> carpetGUI$mouseLeave() {
      return this.carpetGUI$getWrapper().carpetGUI$mouseLeave();
   }

   public boolean carpetGUI$onMouseScroll(double mouseX, double mouseY, double amount) {
      return this.carpetGUI$getWrapper().carpetGUI$onMouseScroll(mouseX, mouseY, amount);
   }

   public boolean carpetGUI$onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.carpetGUI$getWrapper().carpetGUI$onMouseDrag(click, deltaX, deltaY);
   }

   public boolean carpetGUI$onKeyPress(KeyEvent input) {
      return this.carpetGUI$getWrapper().carpetGUI$onKeyPress(input);
   }

   public boolean carpetGUI$onCharTyped(CharacterEvent input) {
      return this.carpetGUI$getWrapper().carpetGUI$onCharTyped(input);
   }

   public boolean carpetGUI$canFocus(UIComponent.FocusSource source) {
      return true;
   }

   public void carpetGUI$onFocusGained(UIComponent.FocusSource source) {
      this.setFocused(source == UIComponent.FocusSource.KEYBOARD_CYCLE);
      this.carpetGUI$getWrapper().carpetGUI$onFocusGained(source);
   }

   public void carpetGUI$onFocusLost() {
      this.setFocused(false);
      this.carpetGUI$getWrapper().carpetGUI$onFocusLost();
   }

   public <C extends UIComponent> C carpetGUI$configure(Consumer<C> closure) {
      return (C)this.carpetGUI$getWrapper().carpetGUI$configure(closure);
   }

   public CursorStyle carpetGUI$cursorStyle() {
      return this.carpetGUI$getWrapper().carpetGUI$cursorStyle();
   }

   public UIComponent carpetGUI$cursorStyle(CursorStyle style) {
      return this.carpetGUI$getWrapper().carpetGUI$cursorStyle(style);
   }

   public UIComponent carpetGUI$tooltip(List<ClientTooltipComponent> tooltip) {
      return this.carpetGUI$getWrapper().carpetGUI$tooltip(tooltip);
   }

   public List<ClientTooltipComponent> carpetGUI$tooltip() {
      return this.carpetGUI$getWrapper().carpetGUI$tooltip();
   }

   public UIComponent carpetGUI$id(@Nullable String id) {
      this.carpetGUI$getWrapper().carpetGUI$id(id);
      return this;
   }

   public @Nullable String carpetGUI$id() {
      return this.carpetGUI$getWrapper().carpetGUI$id();
   }

   @Unique
   protected VanillaWidgetComponent carpetGUI$getWrapper() {
      if (this.wrapper == null) {
         this.wrapper = UIComponents.wrapVanillaWidget((AbstractWidget)(Object)this);
      }

      return this.wrapper;
   }

   public @Nullable VanillaWidgetComponent carpetGUI$widgetWrapper() {
      return this.wrapper;
   }

   public int carpetGUI$xOffset() {
      return 0;
   }

   public int carpetGUI$yOffset() {
      return 0;
   }

   public int carpetGUI$widthOffset() {
      return 0;
   }

   public int carpetGUI$heightOffset() {
      return 0;
   }

   @Inject(
      method = {"setWidth"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void applyWidthToWrapper(int width, CallbackInfo ci) {
      VanillaWidgetComponent wrapper = this.wrapper;
      if (wrapper != null) {
         wrapper.carpetGUI$horizontalSizing(Sizing.fixed(width));
         ci.cancel();
      }

   }

   public void carpetGUI$updateX(int x) {
      this.carpetGUI$getWrapper().carpetGUI$updateX(x);
   }

   public void carpetGUI$updateY(int y) {
      this.carpetGUI$getWrapper().carpetGUI$updateY(y);
   }

   @Unique
   protected CursorStyle carpetGUI$preferredCursorStyle() {
      return CursorStyle.POINTER;
   }

   //? if >=26.1 {
   @Inject(
      method = {"extractRenderState"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/components/AbstractWidget;extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"
)}
   )
   private void setHovered(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (this.wrapper != null) {
         this.isHovered = this.isHovered && this.wrapper.hovered();
      }
   }
   //?} elif >=1.20 {
   /*@Inject(
      method = {"render"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/components/AbstractWidget;renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
)}
   )
   private void setHovered(GuiGraphics extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (this.wrapper != null) {
         this.isHovered = this.isHovered && this.wrapper.hovered();
      }
   }
   *///?} else if > 1.18.2 {
   /*@Inject(
      method = {"render"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/components/AbstractWidget;renderWidget(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"
   )}
   )
   private void setHovered(com.mojang.blaze3d.vertex.PoseStack extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (this.wrapper != null) {
         this.isHovered = this.isHovered && this.wrapper.hovered();
      }
   }*/
   //?} else {
   /*@Inject(
           method = {"render"},
           at = {@At(
                   value = "INVOKE",
                   target = "Lnet/minecraft/client/gui/components/AbstractWidget;renderButton(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"
           )}
   )
   private void setHovered(com.mojang.blaze3d.vertex.PoseStack extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (this.wrapper != null) {
         this.isHovered = this.isHovered && this.wrapper.hovered();
      }
   }
   *///?}
}
