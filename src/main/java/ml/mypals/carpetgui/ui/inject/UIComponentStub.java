package ml.mypals.carpetgui.ui.inject;

import java.util.List;
import java.util.function.Consumer;
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
import ml.mypals.carpetgui.ui.util.EventSource;
import ml.mypals.carpetgui.ui.util.FocusHandler;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

public interface UIComponentStub extends UIComponent {
   default void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      throw new IllegalStateException("Interface stub method called");
   }

   default @Nullable ParentUIComponent parent() {
      throw new IllegalStateException("Interface stub method called");
   }

   default @Nullable FocusHandler focusHandler() {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent positioning(Positioning positioning) {
      throw new IllegalStateException("Interface stub method called");
   }

   default AnimatableProperty<Positioning> positioning() {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent margins(Insets margins) {
      throw new IllegalStateException("Interface stub method called");
   }

   default AnimatableProperty<Insets> margins() {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent horizontalSizing(Sizing horizontalSizing) {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent verticalSizing(Sizing verticalSizing) {
      throw new IllegalStateException("Interface stub method called");
   }

   default AnimatableProperty<Sizing> horizontalSizing() {
      throw new IllegalStateException("Interface stub method called");
   }

   default AnimatableProperty<Sizing> verticalSizing() {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<MouseEnter> mouseEnter() {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<MouseLeave> mouseLeave() {
      throw new IllegalStateException("Interface stub method called");
   }

   default CursorStyle cursorStyle() {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent cursorStyle(CursorStyle style) {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent tooltip(List<ClientTooltipComponent> tooltip) {
      throw new IllegalStateException("Interface stub method called");
   }

   default List<ClientTooltipComponent> tooltip() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void inflate(Size space) {
      throw new IllegalStateException("Interface stub method called");
   }

   default void mount(ParentUIComponent parent, int x, int y) {
      throw new IllegalStateException("Interface stub method called");
   }

   default void dismount(UIComponent.DismountReason reason) {
      throw new IllegalStateException("Interface stub method called");
   }

   default <C extends UIComponent> C configure(Consumer<C> closure) {
      throw new IllegalStateException("Interface stub method called");
   }

   default int width() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int height() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<MouseDown> mouseDown() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean onMouseUp(MouseButtonEvent click) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<MouseUp> mouseUp() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean onMouseScroll(double mouseX, double mouseY, double amount) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<MouseScroll> mouseScroll() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<MouseDrag> mouseDrag() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean onKeyPress(KeyEvent input) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<KeyPress> keyPress() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean onCharTyped(CharacterEvent input) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<CharTyped> charTyped() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void onFocusGained(UIComponent.FocusSource source) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<FocusGained> focusGained() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void onFocusLost() {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventSource<FocusLost> focusLost() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int x() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void updateX(int x) {
      throw new IllegalStateException("Interface stub method called");
   }

   default int y() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void updateY(int y) {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent id(@Nullable String id) {
      throw new IllegalStateException("Interface stub method called");
   }

   default @Nullable String id() {
      throw new IllegalStateException("Interface stub method called");
   }

   default VanillaWidgetComponent widgetWrapper() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int xOffset() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int yOffset() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int widthOffset() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int heightOffset() {
      throw new IllegalStateException("Interface stub method called");
   }
}
