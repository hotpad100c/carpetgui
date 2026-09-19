package ml.mypals.carpetgui.ui.inject;

import java.util.List;
import java.util.function.Consumer;
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
import ml.mypals.carpetgui.ui.util.EventStream;
import ml.mypals.carpetgui.ui.util.FocusHandler;
import ml.mypals.carpetgui.ui.util.Observable;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

public interface UIComponentStub extends UIComponent {
   default void carpetGUI$draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      throw new IllegalStateException("Interface stub method called");
   }

   default @Nullable ParentUIComponent carpetGUI$parent() {
      throw new IllegalStateException("Interface stub method called");
   }

   default @Nullable FocusHandler carpetGUI$focusHandler() {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent carpetGUI$positioning(Positioning positioning) {
      throw new IllegalStateException("Interface stub method called");
   }

   default Observable<Positioning> carpetGUI$positioning() {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent carpetGUI$margins(Insets margins) {
      throw new IllegalStateException("Interface stub method called");
   }

   default Observable<Insets> carpetGUI$margins() {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent carpetGUI$horizontalSizing(Sizing horizontalSizing) {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent carpetGUI$verticalSizing(Sizing verticalSizing) {
      throw new IllegalStateException("Interface stub method called");
   }

   default Observable<Sizing> carpetGUI$horizontalSizing() {
      throw new IllegalStateException("Interface stub method called");
   }

   default Observable<Sizing> carpetGUI$verticalSizing() {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<MouseEnter> carpetGUI$mouseEnter() {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<MouseLeave> carpetGUI$mouseLeave() {
      throw new IllegalStateException("Interface stub method called");
   }

   default CursorStyle carpetGUI$cursorStyle() {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent carpetGUI$cursorStyle(CursorStyle style) {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent carpetGUI$tooltip(List<ClientTooltipComponent> tooltip) {
      throw new IllegalStateException("Interface stub method called");
   }

   default List<ClientTooltipComponent> carpetGUI$tooltip() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void carpetGUI$inflate(Size space) {
      throw new IllegalStateException("Interface stub method called");
   }

   default void carpetGUI$mount(ParentUIComponent parent, int x, int y) {
      throw new IllegalStateException("Interface stub method called");
   }

   default void carpetGUI$dismount(UIComponent.DismountReason reason) {
      throw new IllegalStateException("Interface stub method called");
   }

   default <C extends UIComponent> C carpetGUI$configure(Consumer<C> closure) {
      throw new IllegalStateException("Interface stub method called");
   }

   default int carpetGUI$width() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int carpetGUI$height() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<MouseDown> carpetGUI$mouseDown() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean carpetGUI$onMouseUp(MouseButtonEvent click) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<MouseUp> carpetGUI$mouseUp() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean carpetGUI$onMouseScroll(double mouseX, double mouseY, double amount) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<MouseScroll> carpetGUI$mouseScroll() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean carpetGUI$onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<MouseDrag> carpetGUI$mouseDrag() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean carpetGUI$onKeyPress(KeyEvent input) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<KeyPress> carpetGUI$keyPress() {
      throw new IllegalStateException("Interface stub method called");
   }

   default boolean carpetGUI$onCharTyped(CharacterEvent input) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<CharTyped> carpetGUI$charTyped() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void carpetGUI$onFocusGained(UIComponent.FocusSource source) {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<FocusGained> carpetGUI$focusGained() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void carpetGUI$onFocusLost() {
      throw new IllegalStateException("Interface stub method called");
   }

   default EventStream<FocusLost> carpetGUI$focusLost() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int carpetGUI$x() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void carpetGUI$updateX(int x) {
      throw new IllegalStateException("Interface stub method called");
   }

   default int carpetGUI$y() {
      throw new IllegalStateException("Interface stub method called");
   }

   default void carpetGUI$updateY(int y) {
      throw new IllegalStateException("Interface stub method called");
   }

   default UIComponent carpetGUI$id(@Nullable String id) {
      throw new IllegalStateException("Interface stub method called");
   }

   default @Nullable String carpetGUI$id() {
      throw new IllegalStateException("Interface stub method called");
   }

   default VanillaWidgetComponent carpetGUI$widgetWrapper() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int carpetGUI$xOffset() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int carpetGUI$yOffset() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int carpetGUI$widthOffset() {
      throw new IllegalStateException("Interface stub method called");
   }

   default int carpetGUI$heightOffset() {
      throw new IllegalStateException("Interface stub method called");
   }
}
