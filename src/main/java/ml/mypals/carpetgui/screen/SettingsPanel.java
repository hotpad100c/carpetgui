package ml.mypals.carpetgui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import ml.mypals.carpetgui.ui.component.LabelComponent;
import ml.mypals.carpetgui.ui.component.SliderComponent;
import ml.mypals.carpetgui.ui.component.UIComponents;
import ml.mypals.carpetgui.ui.container.FlowLayout;
import ml.mypals.carpetgui.ui.container.OverlayContainer;
import ml.mypals.carpetgui.ui.container.UIContainers;
import ml.mypals.carpetgui.ui.core.Color;
import ml.mypals.carpetgui.ui.core.HorizontalAlignment;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;

public class SettingsPanel {
   private static OverlayContainer<FlowLayout> current;

   public static void toggle(FlowLayout outline, Runnable onLayoutChanged) {
      if (isOpen(outline.carpetGUI$parent())) {
         close();
      } else {
         open(outline, onLayoutChanged);
      }
   }

   public static boolean isOpen(ParentUIComponent root) {
      return current != null && root != null && current.carpetGUI$parent() == root;
   }

   public static void close() {
      if (current != null) {
         current.carpetGUI$remove();
      }

      current = null;
   }

   private static void open(FlowLayout outline, Runnable onLayoutChanged) {
      ParentUIComponent root = outline.carpetGUI$parent();
      if (root instanceof FlowLayout) {
         current = create(outline, onLayoutChanged);
         ((FlowLayout)root).child(current);
      }
   }

   private static OverlayContainer<FlowLayout> create(FlowLayout outline, Runnable onLayoutChanged) {
      FlowLayout panel = UIContainers.verticalFlow(Sizing.fixed(180), Sizing.content());
      panel.surface(Surface.VANILLA_TRANSLUCENT.and(Surface.outline(2010107855))).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      panel.padding(Insets.of(6));
      panel.gap(4);
      panel.child(UIComponents.label(Component.translatable("gui.settings.title")).color(Color.WHITE).shadow(true));
      Runnable onChanged = () -> {
         ScreenUtils.applyMenuSize(outline);
         onLayoutChanged.run();
      };
      List<Runnable> syncs = new ArrayList();
      panel.child(sliderRow("gui.settings.menu_width", CarpetGUIConfigManager.MIN_MENU_SIZE, CarpetGUIConfigManager.DEFAULT_MENU_SIZE, () -> CarpetGUIConfigManager.menuWidth, CarpetGUIConfigManager::setMenuWidth, onChanged, syncs));
      panel.child(sliderRow("gui.settings.menu_height", CarpetGUIConfigManager.MIN_MENU_SIZE, CarpetGUIConfigManager.DEFAULT_MENU_SIZE, () -> CarpetGUIConfigManager.menuHeight, CarpetGUIConfigManager::setMenuHeight, onChanged, syncs));
      panel.child(sliderRow("gui.settings.category_width", CarpetGUIConfigManager.MIN_CATEGORY_PANEL_WIDTH, CarpetGUIConfigManager.MAX_CATEGORY_PANEL_WIDTH, () -> CarpetGUIConfigManager.categoryPanelWidth, CarpetGUIConfigManager::setCategoryPanelWidth, onChanged, syncs));
      FlowLayout buttons = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
      buttons.gap(6).horizontalAlignment(HorizontalAlignment.CENTER);
      buttons.carpetGUI$margins(Insets.top(4));
      buttons.child(ScreenUtils.btn(Component.translatable("gui.settings.reset"), Sizing.fill(45), Sizing.fill(100), () -> {
         CarpetGUIConfigManager.resetUiConfig();
         syncs.forEach(Runnable::run);
         onChanged.run();
      }));
      buttons.child(ScreenUtils.btn(Component.translatable("gui.settings.close"), Sizing.fill(45), Sizing.fill(100), SettingsPanel::close));
      panel.child(buttons);
      OverlayContainer<FlowLayout> overlay = UIContainers.<FlowLayout>overlay(panel);
      overlay.surface(Surface.flat(0));
      overlay.closeOnClick(true);
      return overlay;
   }

   private static FlowLayout sliderRow(String key, int min, int max, IntSupplier getter, IntConsumer setter, Runnable onChanged, List<Runnable> syncs) {
      FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
      row.verticalAlignment(VerticalAlignment.CENTER);
      FlowLayout labelBox = UIContainers.horizontalFlow(Sizing.fill(42), Sizing.fill(100));
      labelBox.verticalAlignment(VerticalAlignment.CENTER);
      labelBox.child(UIComponents.label(Component.translatable(key)).color(Color.WHITE));
      row.child(labelBox);
      LabelComponent valueLabel = UIComponents.label(percent(getter.getAsInt()));
      valueLabel.color(Color.WHITE);
      SliderComponent slider = UIComponents.slider(Sizing.fill(40), min, max, getter.getAsInt());
      slider.onChanged((value) -> {
         setter.accept(value);
         valueLabel.text(percent(getter.getAsInt()));
         onChanged.run();
      });
      slider.onReleased(CarpetGUIConfigManager::saveUiConfig);
      row.child(slider);
      FlowLayout valueBox = UIContainers.horizontalFlow(Sizing.fill(18), Sizing.fill(100));
      valueBox.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      valueBox.child(valueLabel);
      row.child(valueBox);
      syncs.add(() -> {
         slider.value(getter.getAsInt());
         valueLabel.text(percent(getter.getAsInt()));
      });
      return row;
   }

   private static Component percent(int value) {
      return Component.literal(value + "%");
   }
}
