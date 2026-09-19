package ml.mypals.carpetgui.ui.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

public record Color(float red, float green, float blue, float alpha) {
   public static final Color BLACK = ofRgb(0);
   public static final Color WHITE = ofRgb(16777215);
   public static final Color RED = ofRgb(16711680);
   public static final Color GREEN = ofRgb(65280);
   public static final Color BLUE = ofRgb(255);

   public Color(float red, float green, float blue) {
      this(red, green, blue, 1.0F);
   }

   public static Color ofArgb(int argb) {
      return new Color((float)(argb >> 16 & 255) / 255.0F, (float)(argb >> 8 & 255) / 255.0F, (float)(argb & 255) / 255.0F, (float)(argb >>> 24) / 255.0F);
   }

   public static Color ofRgb(int rgb) {
      return new Color((float)(rgb >> 16 & 255) / 255.0F, (float)(rgb >> 8 & 255) / 255.0F, (float)(rgb & 255) / 255.0F, 1.0F);
   }

   public static Color ofFormatting(@NotNull ChatFormatting formatting) {
      TextColor textColor = TextColor.fromLegacyFormat(formatting);
      return ofRgb(textColor == null ? 0 : textColor.getValue());
   }

   public int rgb() {
      return (int)(this.red * 255.0F) << 16 | (int)(this.green * 255.0F) << 8 | (int)(this.blue * 255.0F);
   }

   public int argb() {
      return (int)(this.alpha * 255.0F) << 24 | (int)(this.red * 255.0F) << 16 | (int)(this.green * 255.0F) << 8 | (int)(this.blue * 255.0F);
   }
}
