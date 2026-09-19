package ml.mypals.carpetgui.ui.core;

public interface Surface {
   Surface BLANK = (context, component) -> {
   };

   Surface VANILLA_TRANSLUCENT = (context, component) -> context.drawGradientRect(component.carpetGUI$x(), component.carpetGUI$y(), component.carpetGUI$width(), component.carpetGUI$height(), -1072689136, -1072689136, -804253680, -804253680);

   static Surface blur(float quality, float size) {
      return flat(1610612736);
   }

   static Surface flat(int color) {
      return (context, component) -> context.fill(component.carpetGUI$x(), component.carpetGUI$y(), component.carpetGUI$x() + component.carpetGUI$width(), component.carpetGUI$y() + component.carpetGUI$height(), color);
   }

   static Surface outline(int color) {
      return (context, component) -> context.drawRectOutline(component.carpetGUI$x(), component.carpetGUI$y(), component.carpetGUI$width(), component.carpetGUI$height(), color);
   }

   void draw(OwoUIGraphics var1, ParentUIComponent var2);

   default Surface and(Surface surface) {
      return (context, component) -> {
         this.draw(context, component);
         surface.draw(context, component);
      };
   }
}
