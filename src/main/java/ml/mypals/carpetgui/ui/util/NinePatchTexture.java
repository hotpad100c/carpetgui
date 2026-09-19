package ml.mypals.carpetgui.ui.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.core.Color;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.PositionedRectangle;
import ml.mypals.carpetgui.ui.core.Size;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NinePatchTexture {
   private final Identifier texture;
   private final int u;
   private final int v;
   private final PatchSizing patchSizing;
   private final Size textureSize;
   private final boolean repeat;
   private static final Map<Identifier, NinePatchTexture> REGISTRY = new HashMap();

   public NinePatchTexture(Identifier texture, int u, int v, PatchSizing patchSizing, Size textureSize, boolean repeat) {
      this.texture = texture;
      this.u = u;
      this.v = v;
      this.textureSize = textureSize;
      this.patchSizing = patchSizing;
      this.repeat = repeat;
   }

   public NinePatchTexture(Identifier texture, int u, int v, Size cornerPatchSize, Size centerPatchSize, Size textureSize, boolean repeat) {
      this(texture, u, v, new PatchSizing((Size)null, cornerPatchSize, centerPatchSize), textureSize, repeat);
   }

   public NinePatchTexture(Identifier texture, int u, int v, Size patchSize, Size textureSize, boolean repeat) {
      this(texture, u, v, new PatchSizing(patchSize, (Size)null, (Size)null), textureSize, repeat);
   }

   private Size cornerPatchSize() {
      return this.patchSizing.cornerPatchSize();
   }

   private Size centerPatchSize() {
      return this.patchSizing.centerPatchSize();
   }

   public void draw(OwoUIGraphics context, PositionedRectangle rectangle) {
      this.draw(context, rectangle, Color.WHITE);
   }

   public void draw(OwoUIGraphics context, PositionedRectangle rectangle, Color color) {
      this.draw(context, rectangle.carpetGUI$x(), rectangle.carpetGUI$y(), rectangle.carpetGUI$width(), rectangle.carpetGUI$height(), color);
   }

   public void draw(OwoUIGraphics context, int x, int y, int width, int height) {
      this.draw(context, x, y, width, height, Color.WHITE);
   }

   public void draw(OwoUIGraphics context, int x, int y, int width, int height, Color color) {
      this.draw(context, RenderPipelines.GUI_TEXTURED, x, y, width, height, color);
   }

   public void draw(OwoUIGraphics context, RenderPipeline pipeline, int x, int y, int width, int height) {
      this.draw(context, pipeline, x, y, width, height, Color.WHITE);
   }

   public void draw(OwoUIGraphics context, RenderPipeline pipeline, int x, int y, int width, int height, Color color) {
      int rightEdge = this.cornerPatchSize().width() + this.centerPatchSize().width();
      int bottomEdge = this.cornerPatchSize().height() + this.centerPatchSize().height();
      context.blit(pipeline, this.texture, x, y, (float)this.u, (float)this.v, this.cornerPatchSize().width(), this.cornerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
      context.blit(pipeline, this.texture, x + width - this.cornerPatchSize().width(), y, (float)(this.u + rightEdge), (float)this.v, this.cornerPatchSize().width(), this.cornerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
      context.blit(pipeline, this.texture, x, y + height - this.cornerPatchSize().height(), (float)this.u, (float)(this.v + bottomEdge), this.cornerPatchSize().width(), this.cornerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
      context.blit(pipeline, this.texture, x + width - this.cornerPatchSize().width(), y + height - this.cornerPatchSize().height(), (float)(this.u + rightEdge), (float)(this.v + bottomEdge), this.cornerPatchSize().width(), this.cornerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
      if (this.repeat) {
         this.drawRepeated(context, pipeline, x, y, width, height, color);
      } else {
         this.drawStretched(context, pipeline, x, y, width, height, color);
      }

   }

   protected void drawStretched(OwoUIGraphics context, RenderPipeline pipeline, int x, int y, int width, int height, Color color) {
      int doubleCornerHeight = this.cornerPatchSize().height() * 2;
      int doubleCornerWidth = this.cornerPatchSize().width() * 2;
      int rightEdge = this.cornerPatchSize().width() + this.centerPatchSize().width();
      int bottomEdge = this.cornerPatchSize().height() + this.centerPatchSize().height();
      if (width > doubleCornerWidth && height > doubleCornerHeight) {
         context.blit(pipeline, this.texture, x + this.cornerPatchSize().width(), y + this.cornerPatchSize().height(), (float)(this.u + this.cornerPatchSize().width()), (float)(this.v + this.cornerPatchSize().height()), width - doubleCornerWidth, height - doubleCornerHeight, this.centerPatchSize().width(), this.centerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
      }

      if (width > doubleCornerWidth) {
         context.blit(pipeline, this.texture, x + this.cornerPatchSize().width(), y, (float)(this.u + this.cornerPatchSize().width()), (float)this.v, width - doubleCornerWidth, this.cornerPatchSize().height(), this.centerPatchSize().width(), this.cornerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
         context.blit(pipeline, this.texture, x + this.cornerPatchSize().width(), y + height - this.cornerPatchSize().height(), (float)(this.u + this.cornerPatchSize().width()), (float)(this.v + bottomEdge), width - doubleCornerWidth, this.cornerPatchSize().height(), this.centerPatchSize().width(), this.cornerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
      }

      if (height > doubleCornerHeight) {
         context.blit(pipeline, this.texture, x, y + this.cornerPatchSize().height(), (float)this.u, (float)(this.v + this.cornerPatchSize().height()), this.cornerPatchSize().width(), height - doubleCornerHeight, this.cornerPatchSize().width(), this.centerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
         context.blit(pipeline, this.texture, x + width - this.cornerPatchSize().width(), y + this.cornerPatchSize().height(), (float)(this.u + rightEdge), (float)(this.v + this.cornerPatchSize().height()), this.cornerPatchSize().width(), height - doubleCornerHeight, this.cornerPatchSize().width(), this.centerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
      }

   }

   protected void drawRepeated(OwoUIGraphics context, RenderPipeline pipeline, int x, int y, int width, int height, Color color) {
      int doubleCornerHeight = this.cornerPatchSize().height() * 2;
      int doubleCornerWidth = this.cornerPatchSize().width() * 2;
      int rightEdge = this.cornerPatchSize().width() + this.centerPatchSize().width();
      int bottomEdge = this.cornerPatchSize().height() + this.centerPatchSize().height();
      if (width > doubleCornerWidth && height > doubleCornerHeight) {
         for(int leftoverHeight = height - doubleCornerHeight; leftoverHeight > 0; leftoverHeight -= this.centerPatchSize().height()) {
            int drawHeight = Math.min(this.centerPatchSize().height(), leftoverHeight);

            for(int leftoverWidth = width - doubleCornerWidth; leftoverWidth > 0; leftoverWidth -= this.centerPatchSize().width()) {
               int drawWidth = Math.min(this.centerPatchSize().width(), leftoverWidth);
               context.blit(pipeline, this.texture, x + this.cornerPatchSize().width() + leftoverWidth - drawWidth, y + this.cornerPatchSize().height() + leftoverHeight - drawHeight, (float)(this.u + this.cornerPatchSize().width() + this.centerPatchSize().width() - drawWidth), (float)(this.v + this.cornerPatchSize().height() + this.centerPatchSize().height() - drawHeight), drawWidth, drawHeight, drawWidth, drawHeight, this.textureSize.width(), this.textureSize.height(), color.argb());
            }
         }
      }

      if (width > doubleCornerWidth) {
         for(int leftoverWidth = width - doubleCornerWidth; leftoverWidth > 0; leftoverWidth -= this.centerPatchSize().width()) {
            int drawWidth = Math.min(this.centerPatchSize().width(), leftoverWidth);
            context.blit(pipeline, this.texture, x + this.cornerPatchSize().width() + leftoverWidth - drawWidth, y, (float)(this.u + this.cornerPatchSize().width() + this.centerPatchSize().width() - drawWidth), (float)this.v, drawWidth, this.cornerPatchSize().height(), drawWidth, this.cornerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
            context.blit(pipeline, this.texture, x + this.cornerPatchSize().width() + leftoverWidth - drawWidth, y + height - this.cornerPatchSize().height(), (float)(this.u + this.cornerPatchSize().width() + this.centerPatchSize().width() - drawWidth), (float)(this.v + bottomEdge), drawWidth, this.cornerPatchSize().height(), drawWidth, this.cornerPatchSize().height(), this.textureSize.width(), this.textureSize.height(), color.argb());
         }
      }

      if (height > doubleCornerHeight) {
         for(int leftoverHeight = height - doubleCornerHeight; leftoverHeight > 0; leftoverHeight -= this.centerPatchSize().height()) {
            int drawHeight = Math.min(this.centerPatchSize().height(), leftoverHeight);
            context.blit(pipeline, this.texture, x, y + this.cornerPatchSize().height() + leftoverHeight - drawHeight, (float)this.u, (float)(this.v + this.cornerPatchSize().height() + this.centerPatchSize().height() - drawHeight), this.cornerPatchSize().width(), drawHeight, this.cornerPatchSize().width(), drawHeight, this.textureSize.width(), this.textureSize.height(), color.argb());
            context.blit(pipeline, this.texture, x + width - this.cornerPatchSize().width(), y + this.cornerPatchSize().height() + leftoverHeight - drawHeight, (float)(this.u + rightEdge), (float)(this.v + this.cornerPatchSize().height() + this.centerPatchSize().height() - drawHeight), this.cornerPatchSize().width(), drawHeight, this.cornerPatchSize().width(), drawHeight, this.textureSize.width(), this.textureSize.height(), color.argb());
         }
      }

   }

   public static void draw(Identifier texture, OwoUIGraphics context, int x, int y, int width, int height) {
      draw(texture, context, RenderPipelines.GUI_TEXTURED, x, y, width, height);
   }

   public static void draw(Identifier texture, OwoUIGraphics context, int x, int y, int width, int height, Color color) {
      draw(texture, context, RenderPipelines.GUI_TEXTURED, x, y, width, height, color);
   }

   public static void draw(Identifier texture, OwoUIGraphics context, RenderPipeline pipeline, int x, int y, int width, int height) {
      ifPresent(texture, (ninePatchTexture) -> ninePatchTexture.draw(context, pipeline, x, y, width, height));
   }

   public static void draw(Identifier texture, OwoUIGraphics context, RenderPipeline pipeline, int x, int y, int width, int height, Color color) {
      ifPresent(texture, (ninePatchTexture) -> ninePatchTexture.draw(context, pipeline, x, y, width, height, color));
   }

   public static void draw(Identifier texture, OwoUIGraphics context, PositionedRectangle rectangle) {
      ifPresent(texture, (ninePatchTexture) -> ninePatchTexture.draw(context, rectangle));
   }

   public static void draw(Identifier texture, OwoUIGraphics context, PositionedRectangle rectangle, Color color) {
      ifPresent(texture, (ninePatchTexture) -> ninePatchTexture.draw(context, rectangle, color));
   }

   private static void ifPresent(Identifier texture, Consumer<NinePatchTexture> action) {
      NinePatchTexture patch = (NinePatchTexture)REGISTRY.get(texture);
      if (patch != null) {
         action.accept(patch);
      }
   }

   private static void register(String id, String texture, int u, int v, Size cornerPatchSize, Size centerPatchSize, Size textureSize, boolean repeat) {
      REGISTRY.put(UI.id(id), new NinePatchTexture(UI.id("textures/gui/" + texture), u, v, cornerPatchSize, centerPatchSize, textureSize, repeat));
   }

   static {
      Size buttons = Size.of(64, 192);
      register("button/active", "buttons.png", 0, 0, Size.of(3, 3), Size.of(58, 58), buttons, true);
      register("button/hovered", "buttons.png", 0, 64, Size.of(3, 3), Size.of(58, 58), buttons, true);
      register("button/disabled", "buttons.png", 0, 128, Size.of(3, 3), Size.of(58, 58), buttons, true);
   }

   public static record PatchSizing(@Nullable Size patchSize, @Nullable Size cornerPatchSize, @Nullable Size centerPatchSize) {
      public PatchSizing(@Nullable Size patchSize, @Nullable Size cornerPatchSize, @Nullable Size centerPatchSize) {
         if (patchSize == null) {
            if (cornerPatchSize != null && centerPatchSize == null) {
               throw new IllegalStateException("Missing center Patch Size while providing corner Patch Size!");
            }

            if (cornerPatchSize == null && centerPatchSize != null) {
               throw new IllegalStateException("Missing corner Patch Size while providing center Patch Size!");
            }

            if (cornerPatchSize == null && centerPatchSize == null) {
               throw new IllegalStateException("Missing base patch Size or patch size for both corner and center!");
            }
         }

         this.patchSize = patchSize;
         this.cornerPatchSize = cornerPatchSize;
         this.centerPatchSize = centerPatchSize;
      }

      public @NotNull Size cornerPatchSize() {
         return this.cornerPatchSize != null ? this.cornerPatchSize : this.patchSize;
      }

      public @NotNull Size centerPatchSize() {
         return this.centerPatchSize != null ? this.centerPatchSize : this.patchSize;
      }
   }
}
