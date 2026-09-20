package ml.mypals.carpetgui.ui.component;

import java.util.List;
import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.container.FlowLayout;
import ml.mypals.carpetgui.ui.container.UIContainers;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.UIComponent;
//? if >=1.21.9 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?} else {
import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;

public class DropdownComponent extends FlowLayout {
   protected final FlowLayout entries;
   protected boolean closeWhenNotHovered = false;

   protected DropdownComponent(Sizing horizontalSizing) {
      super(Sizing.content(), Sizing.content(), FlowLayout.Algorithm.HORIZONTAL);
      this.entries = UIContainers.verticalFlow(horizontalSizing, Sizing.content());
      this.entries.padding(Insets.of(1));
      this.entries.allowOverflow(true);
      this.entries.surface(Surface.flat(-956301312).and(Surface.blur(3.0F, 5.0F)).and(Surface.outline(-15592942)));
      this.child(this.entries);
   }

   public ParentUIComponent surface(Surface surface) {
      this.entries.surface(surface);
      return this;
   }

   public void carpetGUI$draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      super.carpetGUI$draw(graphics, mouseX, mouseY, partialTicks, delta);
      if (this.closeWhenNotHovered && !this.isInBoundingBox((double)mouseX, (double)mouseY)) {
         this.queue(() -> {
            this.closeWhenNotHovered(false);
            this.parent.removeChild(this);
         });
      }
   }

   public void layout(Size space) {
      super.layout(space);
      List<UIComponent> entries = this.entries.children();

      for(int i = 0; i < entries.size(); ++i) {
         UIComponent entry = entries.get(i);
         if (entry instanceof ResizeableComponent sizeable) {
            sizeable.setWidth(this.entries.carpetGUI$width() - ((Insets)this.entries.padding().get()).horizontal() - ((Insets)entry.carpetGUI$margins().get()).horizontal());
         }
      }
   }

   public DropdownComponent button(Component text, Consumer<DropdownComponent> onClick) {
      this.entries.child((new Button(this, text, onClick)).carpetGUI$margins(Insets.of(2)));
      return this;
   }

   public FlowLayout removeChild(UIComponent child) {
      if (child == this.entries) {
         this.queue(() -> {
            this.closeWhenNotHovered(false);
            this.parent.removeChild(this);
         });
      }

      return super.removeChild(child);
   }

   public DropdownComponent closeWhenNotHovered(boolean closeWhenNotHovered) {
      this.closeWhenNotHovered = closeWhenNotHovered;
      return this;
   }

   public boolean closeWhenNotHovered() {
      return this.closeWhenNotHovered;
   }

   protected static class Button extends LabelComponent implements ResizeableComponent {
      protected final DropdownComponent parentDropdown;
      protected Consumer<DropdownComponent> onClick;

      protected Button(DropdownComponent parentDropdown, Component text, Consumer<DropdownComponent> onClick) {
         super(text);
         this.onClick = onClick;
         this.parentDropdown = parentDropdown;
         this.carpetGUI$margins(Insets.vertical(1));
         this.carpetGUI$cursorStyle(CursorStyle.HAND);
      }

      public void setWidth(int width) {
         this.width = width;
      }

      public boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
         super.carpetGUI$onMouseDown(click, doubled);
         this.onClick.accept(this.parentDropdown);
         this.playInteractionSound();
         return true;
      }

      public void carpetGUI$draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
         if (this.isInBoundingBox((double)mouseX, (double)mouseY)) {
            Insets margins = (Insets)this.margins.get();
            graphics.fill(this.x - margins.left(), this.y - margins.top(), this.x + this.width + margins.right(), this.y + this.height + margins.bottom(), 1157627903);
         }

         super.carpetGUI$draw(graphics, mouseX, mouseY, partialTicks, delta);
      }

      protected void playInteractionSound() {
         UI.Sounds.playButtonSound();
      }
   }

   protected interface ResizeableComponent {
      void setWidth(int var1);
   }
}
