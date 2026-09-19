package ml.mypals.carpetgui.ui.component;

import ml.mypals.carpetgui.ui.base.BaseUIComponent;
import ml.mypals.carpetgui.ui.core.AnimatableProperty;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.PositionedRectangle;
import ml.mypals.carpetgui.ui.core.Sizing;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public class TextureComponent extends BaseUIComponent {
   protected final Identifier texture;
   protected final int u;
   protected final int v;
   protected final int regionWidth;
   protected final int regionHeight;
   protected final int textureWidth;
   protected final int textureHeight;
   protected final AnimatableProperty<PositionedRectangle> visibleArea;
   protected boolean blend = false;

   protected TextureComponent(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
      this.texture = texture;
      this.u = u;
      this.v = v;
      this.regionWidth = regionWidth;
      this.regionHeight = regionHeight;
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
      this.visibleArea = AnimatableProperty.<PositionedRectangle>of(PositionedRectangle.of(0, 0, this.regionWidth, this.regionHeight));
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      return this.regionWidth;
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      return this.regionHeight;
   }

   public void update(float delta, int mouseX, int mouseY) {
      super.update(delta, mouseX, mouseY);
      this.visibleArea.update(delta);
   }

   public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      Matrix3x2fStack matrices = graphics.pose();
      matrices.pushMatrix();
      matrices.translate((float)this.x, (float)this.y);
      matrices.scale((float)this.width / (float)this.regionWidth, (float)this.height / (float)this.regionHeight);
      PositionedRectangle visibleArea = (PositionedRectangle)this.visibleArea.get();
      int bottomEdge = Math.min(visibleArea.y() + visibleArea.height(), this.regionHeight);
      int rightEdge = Math.min(visibleArea.x() + visibleArea.width(), this.regionWidth);
      graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, visibleArea.x(), visibleArea.y(), (float)(this.u + visibleArea.x()), (float)(this.v + visibleArea.y()), rightEdge - visibleArea.x(), bottomEdge - visibleArea.y(), rightEdge - visibleArea.x(), bottomEdge - visibleArea.y(), this.textureWidth, this.textureHeight);
      matrices.popMatrix();
   }

   public TextureComponent visibleArea(PositionedRectangle visibleArea) {
      this.visibleArea.set(visibleArea);
      return this;
   }

   public TextureComponent resetVisibleArea() {
      this.visibleArea(PositionedRectangle.of(0, 0, this.regionWidth, this.regionHeight));
      return this;
   }

   public AnimatableProperty<PositionedRectangle> visibleArea() {
      return this.visibleArea;
   }

   public TextureComponent blend(boolean blend) {
      this.blend = blend;
      return this;
   }

   public boolean blend() {
      return this.blend;
   }
}
