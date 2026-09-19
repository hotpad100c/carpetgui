package ml.mypals.carpetgui.ui.core;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

public record Color(float red, float green, float blue, float alpha) {
   public static final Color BLACK = ofRgb(0);
   public static final Color WHITE = ofRgb(16777215);
   public static final Color RED = ofRgb(16711680);
   public static final Color GREEN = ofRgb(65280);
   public static final Color BLUE = ofRgb(255);
   private static final Map<String, Color> NAMED_TEXT_COLORS = (Map)Stream.of(ChatFormatting.values()).filter((formatting) -> TextColor.fromLegacyFormat(formatting) != null).collect(ImmutableMap.toImmutableMap((formatting) -> formatting.name().toLowerCase(Locale.ROOT).replace("_", "-"), Color::ofFormatting));

   public Color(float red, float green, float blue) {
      this(red, green, blue, 1.0F);
   }

   public static Color ofArgb(int argb) {
      return new Color((float)(argb >> 16 & 255) / 255.0F, (float)(argb >> 8 & 255) / 255.0F, (float)(argb & 255) / 255.0F, (float)(argb >>> 24) / 255.0F);
   }

   public static Color ofRgb(int rgb) {
      return new Color((float)(rgb >> 16 & 255) / 255.0F, (float)(rgb >> 8 & 255) / 255.0F, (float)(rgb & 255) / 255.0F, 1.0F);
   }

   public static Color ofHsv(float hue, float saturation, float value) {
      return ofRgb(Mth.hsvToRgb(hue - 5.0E-8F, saturation, value));
   }

   public static Color ofHsv(float hue, float saturation, float value, float alpha) {
      return ofArgb((int)(alpha * 255.0F) << 24 | Mth.hsvToRgb(hue - 5.0E-8F, saturation, value));
   }

   public static Color ofFormatting(@NotNull ChatFormatting formatting) {
      TextColor textColor = TextColor.fromLegacyFormat(formatting);
      return ofRgb(textColor == null ? 0 : textColor.getValue());
   }

   public static Color ofDye(@NotNull DyeColor dyeColor) {
      return ofArgb(dyeColor.getTextureDiffuseColor());
   }

   public static Color random() {
      return ofArgb((int)(Math.random() * (double)1.6777215E7F) | -16777216);
   }

   public int rgb() {
      return (int)(this.red * 255.0F) << 16 | (int)(this.green * 255.0F) << 8 | (int)(this.blue * 255.0F);
   }

   public int argb() {
      return (int)(this.alpha * 255.0F) << 24 | (int)(this.red * 255.0F) << 16 | (int)(this.green * 255.0F) << 8 | (int)(this.blue * 255.0F);
   }

   public float[] hsv() {
      float cmax = Math.max(Math.max(this.red, this.green), this.blue);
      float cmin = Math.min(Math.min(this.red, this.green), this.blue);
      float saturation;
      if (cmax != 0.0F) {
         saturation = (cmax - cmin) / cmax;
      } else {
         saturation = 0.0F;
      }

      float hue;
      if (saturation == 0.0F) {
         hue = 0.0F;
      } else {
         float redc = (cmax - this.red) / (cmax - cmin);
         float greenc = (cmax - this.green) / (cmax - cmin);
         float bluec = (cmax - this.blue) / (cmax - cmin);
         if (this.red == cmax) {
            hue = bluec - greenc;
         } else if (this.green == cmax) {
            hue = 2.0F + redc - bluec;
         } else {
            hue = 4.0F + greenc - redc;
         }

         hue /= 6.0F;
         if (hue < 0.0F) {
            ++hue;
         }
      }

      return new float[]{hue, saturation, cmax, this.alpha};
   }

   public String asHexString(boolean includeAlpha) {
      return includeAlpha ? String.format("#%08X", this.argb()) : String.format("#%06X", this.rgb());
   }

   public Color interpolate(Color next, float delta) {
      return new Color(Mth.lerp(delta, this.red, next.red), Mth.lerp(delta, this.green, next.green), Mth.lerp(delta, this.blue, next.blue), Mth.lerp(delta, this.alpha, next.alpha));
   }
}
