package ml.mypals.carpetgui.ui.container;

import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.UIComponent;

public final class UIContainers {
   private UIContainers() {
   }

   public static FlowLayout verticalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
      return new FlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.VERTICAL);
   }

   public static FlowLayout horizontalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
      return new FlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.HORIZONTAL);
   }


   public static <C extends UIComponent> ScrollContainer<C> verticalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child) {
      return new ScrollContainer<C>(ScrollContainer.ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child);
   }

   public static <C extends UIComponent> OverlayContainer<C> overlay(C child) {
      return new OverlayContainer<C>(child);
   }
}
