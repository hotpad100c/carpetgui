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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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

   private static final int SLIDER_WIDTH = 80;
   private static final int COLUMN_GAP = 6;
   private static final String[] SLIDER_KEYS = new String[]{"gui.settings.ui_scale", "gui.settings.menu_width", "gui.settings.menu_height", "gui.settings.category_width", "gui.settings.input_width"};

   private static OverlayContainer<FlowLayout> create(FlowLayout outline, Runnable onLayoutChanged) {
      Font font = Minecraft.getInstance().font;
      int labelWidth = 0;
      for(String key : SLIDER_KEYS) {
         labelWidth = Math.max(labelWidth, font.width(Component.translatable(key)));
      }

      int valueWidth = font.width(percent(CarpetGUIConfigManager.MAX_UI_SCALE));
      int innerWidth = labelWidth + SLIDER_WIDTH + valueWidth + COLUMN_GAP * 2;
      FlowLayout panel = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
      panel.surface(Surface.VANILLA_TRANSLUCENT.and(Surface.outline(2010107855))).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      panel.padding(Insets.of(6));
      panel.gap(4);
      panel.child(UIComponents.label(Component.translatable("gui.settings.title")).color(Color.WHITE).shadow(true));
      Runnable onChanged = () -> {
         ScreenUtils.applyMenuSize(outline);
         onLayoutChanged.run();
      };
      List<Runnable> syncs = new ArrayList();
      panel.child(sliderRow(labelWidth, valueWidth, "gui.settings.ui_scale", CarpetGUIConfigManager.MIN_UI_SCALE, CarpetGUIConfigManager.MAX_UI_SCALE, () -> CarpetGUIConfigManager.uiScale, CarpetGUIConfigManager::setUiScale, () -> {}, syncs, true));
      panel.child(sliderRow(labelWidth, valueWidth, "gui.settings.menu_width", CarpetGUIConfigManager.MIN_MENU_SIZE, CarpetGUIConfigManager.DEFAULT_MENU_SIZE, () -> CarpetGUIConfigManager.menuWidth, CarpetGUIConfigManager::setMenuWidth, onChanged, syncs, false));
      panel.child(sliderRow(labelWidth, valueWidth, "gui.settings.menu_height", CarpetGUIConfigManager.MIN_MENU_SIZE, CarpetGUIConfigManager.DEFAULT_MENU_SIZE, () -> CarpetGUIConfigManager.menuHeight, CarpetGUIConfigManager::setMenuHeight, onChanged, syncs, false));
      panel.child(sliderRow(labelWidth, valueWidth, "gui.settings.category_width", CarpetGUIConfigManager.MIN_CATEGORY_PANEL_WIDTH, CarpetGUIConfigManager.MAX_CATEGORY_PANEL_WIDTH, () -> CarpetGUIConfigManager.categoryPanelWidth, CarpetGUIConfigManager::setCategoryPanelWidth, onChanged, syncs, false));
      panel.child(sliderRow(labelWidth, valueWidth, "gui.settings.input_width", CarpetGUIConfigManager.MIN_INPUT_WIDTH, CarpetGUIConfigManager.MAX_INPUT_WIDTH, () -> CarpetGUIConfigManager.inputWidth, CarpetGUIConfigManager::setInputWidth, onChanged, syncs, false));
      int buttonWidth = (innerWidth - COLUMN_GAP) / 2;
      FlowLayout buttons = UIContainers.horizontalFlow(Sizing.fixed(innerWidth), Sizing.fixed(16));
      buttons.gap(COLUMN_GAP).horizontalAlignment(HorizontalAlignment.CENTER);
      buttons.carpetGUI$margins(Insets.top(4));
      buttons.child(ScreenUtils.btn(Component.translatable("gui.settings.reset"), Sizing.fixed(buttonWidth), Sizing.fill(100), () -> {
         CarpetGUIConfigManager.resetUiConfig();
         syncs.forEach(Runnable::run);
         onChanged.run();
      }));
      buttons.child(ScreenUtils.btn(Component.translatable("gui.settings.close"), Sizing.fixed(buttonWidth), Sizing.fill(100), SettingsPanel::close));
      panel.child(buttons);
      OverlayContainer<FlowLayout> overlay = UIContainers.<FlowLayout>overlay(panel);
      overlay.surface(Surface.flat(0));
      overlay.closeOnClick(true);
      return overlay;
   }

   private static FlowLayout sliderRow(int labelWidth, int valueWidth, String key, int min, int max, IntSupplier getter, IntConsumer setter, Runnable onChanged, List<Runnable> syncs, boolean applyOnRelease) {
      FlowLayout row = UIContainers.horizontalFlow(Sizing.content(), Sizing.fixed(14));
      row.verticalAlignment(VerticalAlignment.CENTER);
      row.gap(COLUMN_GAP);
      FlowLayout labelBox = UIContainers.horizontalFlow(Sizing.fixed(labelWidth), Sizing.fill(100));
      labelBox.verticalAlignment(VerticalAlignment.CENTER);
      labelBox.child(UIComponents.label(Component.translatable(key)).color(Color.WHITE));
      row.child(labelBox);
      LabelComponent valueLabel = UIComponents.label(percent(getter.getAsInt()));
      valueLabel.color(Color.WHITE);
      SliderComponent slider = UIComponents.slider(Sizing.fixed(SLIDER_WIDTH), min, max, getter.getAsInt());
      slider.onChanged((value) -> {
         if (applyOnRelease) {
            valueLabel.text(percent(value));
         } else {
            setter.accept(value);
            valueLabel.text(percent(getter.getAsInt()));
            onChanged.run();
         }
      });
      slider.onReleased(() -> {
         if (applyOnRelease) {
            setter.accept(slider.value());
            onChanged.run();
         }

         CarpetGUIConfigManager.saveUiConfig();
      });
      row.child(slider);
      FlowLayout valueBox = UIContainers.horizontalFlow(Sizing.fixed(valueWidth), Sizing.fill(100));
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
