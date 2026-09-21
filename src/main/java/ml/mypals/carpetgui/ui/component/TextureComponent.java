package ml.mypals.carpetgui.ui.component;

import ml.mypals.carpetgui.ui.base.BaseUIComponent;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.PositionedRectangle;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.util.Observable;
import net.minecraft.resources.Identifier;

public class TextureComponent extends BaseUIComponent {
   protected final Identifier texture;
   protected final int u;
   protected final int v;
   protected final int regionWidth;
   protected final int regionHeight;
   protected final int textureWidth;
   protected final int textureHeight;
   protected final Observable<PositionedRectangle> visibleArea;
   protected boolean blend = false;

   protected TextureComponent(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
      this.texture = texture;
      this.u = u;
      this.v = v;
      this.regionWidth = regionWidth;
      this.regionHeight = regionHeight;
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
      this.visibleArea = Observable.<PositionedRectangle>of(PositionedRectangle.of(0, 0, this.regionWidth, this.regionHeight));
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      return this.regionWidth;
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      return this.regionHeight;
   }

   public void carpetGUI$draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      graphics.push();
      graphics.translate((float)this.x, (float)this.y);
      graphics.scale((float)this.width / (float)this.regionWidth, (float)this.height / (float)this.regionHeight);
      PositionedRectangle visibleArea = (PositionedRectangle)this.visibleArea.get();
      int bottomEdge = Math.min(visibleArea.carpetGUI$y() + visibleArea.carpetGUI$height(), this.regionHeight);
      int rightEdge = Math.min(visibleArea.carpetGUI$x() + visibleArea.carpetGUI$width(), this.regionWidth);
      graphics.blitTexture(this.texture, visibleArea.carpetGUI$x(), visibleArea.carpetGUI$y(), (float)(this.u + visibleArea.carpetGUI$x()), (float)(this.v + visibleArea.carpetGUI$y()), rightEdge - visibleArea.carpetGUI$x(), bottomEdge - visibleArea.carpetGUI$y(), rightEdge - visibleArea.carpetGUI$x(), bottomEdge - visibleArea.carpetGUI$y(), this.textureWidth, this.textureHeight);
      graphics.pop();
   }

   public TextureComponent visibleArea(PositionedRectangle visibleArea) {
      this.visibleArea.set(visibleArea);
      return this;
   }

   public TextureComponent resetVisibleArea() {
      this.visibleArea(PositionedRectangle.of(0, 0, this.regionWidth, this.regionHeight));
      return this;
   }

   public Observable<PositionedRectangle> visibleArea() {
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
