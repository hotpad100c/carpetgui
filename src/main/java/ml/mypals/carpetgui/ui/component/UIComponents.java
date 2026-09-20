package ml.mypals.carpetgui.ui.component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import ml.mypals.carpetgui.ui.container.FlowLayout;
import ml.mypals.carpetgui.ui.container.UIContainers;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.UIComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class UIComponents {
   private UIComponents() {
   }

   public static ButtonComponent button(Component message, Consumer<ButtonComponent> onPress) {
      return new ButtonComponent(message, onPress);
   }

   public static TextBoxComponent textBox(Sizing horizontalSizing) {
      return new TextBoxComponent(horizontalSizing);
   }

   public static TextBoxComponent textBox(Sizing horizontalSizing, String text) {
      TextBoxComponent textBox = new TextBoxComponent(horizontalSizing);
      textBox.text(text);
      return textBox;
   }

   public static LabelComponent label(Component text) {
      return new LabelComponent(text);
   }

   public static TextureComponent texture(ResourceLocation texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
      return new TextureComponent(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
   }

   public static TextureComponent texture(ResourceLocation texture, int u, int v, int regionWidth, int regionHeight) {
      return new TextureComponent(texture, u, v, regionWidth, regionHeight, 256, 256);
   }

   public static DropdownComponent dropdown(Sizing horizontalSizing) {
      return new DropdownComponent(horizontalSizing);
   }


   public static <T, C extends UIComponent> FlowLayout list(List<T> data, Consumer<FlowLayout> layoutConfigurator, Function<T, C> componentMaker, boolean vertical) {
      FlowLayout layout = vertical ? UIContainers.verticalFlow(Sizing.content(), Sizing.content()) : UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
      layoutConfigurator.accept(layout);

      for(T value : data) {
         layout.child((UIComponent)componentMaker.apply(value));
      }

      return layout;
   }

   public static VanillaWidgetComponent wrapVanillaWidget(AbstractWidget widget) {
      return new VanillaWidgetComponent(widget);
   }
}
