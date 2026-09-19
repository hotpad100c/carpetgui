package ml.mypals.carpetgui.mixin.ui;

import java.util.List;
import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.component.UIComponents;
import ml.mypals.carpetgui.ui.component.VanillaWidgetComponent;
import ml.mypals.carpetgui.ui.core.AnimatableProperty;
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
import ml.mypals.carpetgui.ui.util.EventSource;
import ml.mypals.carpetgui.ui.util.FocusHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
   @Unique
   protected VanillaWidgetComponent wrapper = null;

   public void inflate(Size space) {
      this.carpetGUI$getWrapper().inflate(space);
   }

   public void mount(ParentUIComponent parent, int x, int y) {
      this.carpetGUI$getWrapper().mount(parent, x, y);
   }

   public void dismount(UIComponent.DismountReason reason) {
      this.carpetGUI$getWrapper().dismount(reason);
   }

   public @Nullable ParentUIComponent parent() {
      return this.carpetGUI$getWrapper().parent();
   }

   public @Nullable FocusHandler focusHandler() {
      return this.carpetGUI$getWrapper().focusHandler();
   }

   public UIComponent positioning(Positioning positioning) {
      this.carpetGUI$getWrapper().positioning(positioning);
      return this;
   }

   public AnimatableProperty<Positioning> positioning() {
      return this.carpetGUI$getWrapper().positioning();
   }

   public UIComponent margins(Insets margins) {
      this.carpetGUI$getWrapper().margins(margins);
      return this;
   }

   public AnimatableProperty<Insets> margins() {
      return this.carpetGUI$getWrapper().margins();
   }

   public UIComponent horizontalSizing(Sizing horizontalSizing) {
      this.carpetGUI$getWrapper().horizontalSizing(horizontalSizing);
      return this;
   }

   public UIComponent verticalSizing(Sizing verticalSizing) {
      this.carpetGUI$getWrapper().verticalSizing(verticalSizing);
      return this;
   }

   public AnimatableProperty<Sizing> horizontalSizing() {
      return this.carpetGUI$getWrapper().horizontalSizing();
   }

   public AnimatableProperty<Sizing> verticalSizing() {
      return this.carpetGUI$getWrapper().verticalSizing();
   }

   public EventSource<MouseDown> mouseDown() {
      return this.carpetGUI$getWrapper().mouseDown();
   }

   public int x() {
      return this.carpetGUI$getWrapper().x();
   }

   public int y() {
      return this.carpetGUI$getWrapper().y();
   }

   public int width() {
      return this.carpetGUI$getWrapper().width();
   }

   public int height() {
      return this.carpetGUI$getWrapper().height();
   }

   public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      this.carpetGUI$getWrapper().draw(graphics, mouseX, mouseY, partialTicks, delta);
   }

   public boolean shouldDrawTooltip(double mouseX, double mouseY) {
      return this.carpetGUI$getWrapper().shouldDrawTooltip(mouseX, mouseY);
   }

   public void update(float delta, int mouseX, int mouseY) {
      this.carpetGUI$getWrapper().update(delta, mouseX, mouseY);
      this.cursorStyle(this.active ? this.carpetGUI$preferredCursorStyle() : CursorStyle.POINTER);
   }

   public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
      return this.carpetGUI$getWrapper().onMouseDown(click, doubled);
   }

   public boolean onMouseUp(MouseButtonEvent click) {
      return this.carpetGUI$getWrapper().onMouseUp(click);
   }

   public EventSource<MouseUp> mouseUp() {
      return this.carpetGUI$getWrapper().mouseUp();
   }

   public EventSource<MouseScroll> mouseScroll() {
      return this.carpetGUI$getWrapper().mouseScroll();
   }

   public EventSource<MouseDrag> mouseDrag() {
      return this.carpetGUI$getWrapper().mouseDrag();
   }

   public EventSource<KeyPress> keyPress() {
      return this.carpetGUI$getWrapper().keyPress();
   }

   public EventSource<CharTyped> charTyped() {
      return this.carpetGUI$getWrapper().charTyped();
   }

   public EventSource<FocusGained> focusGained() {
      return this.carpetGUI$getWrapper().focusGained();
   }

   public EventSource<FocusLost> focusLost() {
      return this.carpetGUI$getWrapper().focusLost();
   }

   public EventSource<MouseEnter> mouseEnter() {
      return this.carpetGUI$getWrapper().mouseEnter();
   }

   public EventSource<MouseLeave> mouseLeave() {
      return this.carpetGUI$getWrapper().mouseLeave();
   }

   public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
      return this.carpetGUI$getWrapper().onMouseScroll(mouseX, mouseY, amount);
   }

   public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.carpetGUI$getWrapper().onMouseDrag(click, deltaX, deltaY);
   }

   public boolean onKeyPress(KeyEvent input) {
      return this.carpetGUI$getWrapper().onKeyPress(input);
   }

   public boolean onCharTyped(CharacterEvent input) {
      return this.carpetGUI$getWrapper().onCharTyped(input);
   }

   public boolean canFocus(UIComponent.FocusSource source) {
      return true;
   }

   public void onFocusGained(UIComponent.FocusSource source) {
      this.setFocused(source == UIComponent.FocusSource.KEYBOARD_CYCLE);
      this.carpetGUI$getWrapper().onFocusGained(source);
   }

   public void onFocusLost() {
      this.setFocused(false);
      this.carpetGUI$getWrapper().onFocusLost();
   }

   public <C extends UIComponent> C configure(Consumer<C> closure) {
      return (C)this.carpetGUI$getWrapper().configure(closure);
   }

   public CursorStyle cursorStyle() {
      return this.carpetGUI$getWrapper().cursorStyle();
   }

   public UIComponent cursorStyle(CursorStyle style) {
      return this.carpetGUI$getWrapper().cursorStyle(style);
   }

   public UIComponent tooltip(List<ClientTooltipComponent> tooltip) {
      return this.carpetGUI$getWrapper().tooltip(tooltip);
   }

   public List<ClientTooltipComponent> tooltip() {
      return this.carpetGUI$getWrapper().tooltip();
   }

   public UIComponent id(@Nullable String id) {
      this.carpetGUI$getWrapper().id(id);
      return this;
   }

   public @Nullable String id() {
      return this.carpetGUI$getWrapper().id();
   }

   @Unique
   protected VanillaWidgetComponent carpetGUI$getWrapper() {
      if (this.wrapper == null) {
         this.wrapper = UIComponents.wrapVanillaWidget((AbstractWidget)(Object)this);
      }

      return this.wrapper;
   }

   public @Nullable VanillaWidgetComponent widgetWrapper() {
      return this.wrapper;
   }

   public int xOffset() {
      return 0;
   }

   public int yOffset() {
      return 0;
   }

   public int widthOffset() {
      return 0;
   }

   public int heightOffset() {
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
         wrapper.horizontalSizing(Sizing.fixed(width));
         ci.cancel();
      }

   }

   public void updateX(int x) {
      this.carpetGUI$getWrapper().updateX(x);
   }

   public void updateY(int y) {
      this.carpetGUI$getWrapper().updateY(y);
   }

   protected CursorStyle carpetGUI$preferredCursorStyle() {
      return CursorStyle.POINTER;
   }

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
}
