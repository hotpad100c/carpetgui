package ml.mypals.carpetgui.ui.component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.base.BaseUIComponent;
import ml.mypals.carpetgui.ui.container.FlowLayout;
import ml.mypals.carpetgui.ui.container.UIContainers;
import ml.mypals.carpetgui.ui.core.Color;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.PositionedRectangle;
import ml.mypals.carpetgui.ui.core.Positioning;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.event.UIEvents.MouseEnter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class DropdownComponent extends FlowLayout {
   protected static final Identifier ICONS_TEXTURE = UI.id("textures/gui/dropdown_icons.png");
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

   public static <R extends ParentUIComponent> DropdownComponent openContextMenu(Screen screen, R rootComponent, BiConsumer<R, DropdownComponent> mountFunction, double mouseX, double mouseY, Consumer<DropdownComponent> builder) {
      DropdownComponent dropdown = new DropdownComponent(Sizing.content());
      builder.accept(dropdown);
      mountFunction.accept(rootComponent, dropdown);
      int xLocation = (int)mouseX - rootComponent.x();
      int yLocation = (int)mouseY - rootComponent.y();
      if (xLocation + dropdown.width() > screen.width) {
         xLocation -= xLocation + dropdown.width() - screen.width;
      }

      if (yLocation + dropdown.height() > screen.height) {
         yLocation -= yLocation + dropdown.height() - screen.height;
      }

      dropdown.positioning(Positioning.absolute(xLocation, yLocation));
      MutableBoolean dismounted = new MutableBoolean(false);
      ScreenMouseEvents.beforeMouseClick(screen).register((ScreenMouseEvents.BeforeMouseClick)(screen_, click) -> {
         if (!dismounted.isTrue() && !dropdown.isInBoundingBox(click.x(), click.y())) {
            rootComponent.removeChild(dropdown);
            dismounted.setTrue();
         }
      });
      return dropdown;
   }

   public ParentUIComponent surface(Surface surface) {
      this.entries.surface(surface);
      return this;
   }

   public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      super.draw(graphics, mouseX, mouseY, partialTicks, delta);
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
         UIComponent entry = (UIComponent)entries.get(i);
         if (entry instanceof ResizeableComponent sizeable) {
            sizeable.setWidth(this.entries.width() - ((Insets)this.entries.padding().get()).horizontal() - ((Insets)entry.margins().get()).horizontal());
         }
      }

   }

   public DropdownComponent divider() {
      this.entries.child(new Divider());
      return this;
   }

   public DropdownComponent text(Component text) {
      this.entries.child(UIComponents.label(text).color(Color.ofFormatting(ChatFormatting.GRAY)).margins(Insets.of(2)));
      return this;
   }

   public DropdownComponent button(Component text, Consumer<DropdownComponent> onClick) {
      this.entries.child((new Button(this, text, onClick)).margins(Insets.of(2)));
      return this;
   }

   public DropdownComponent checkbox(Component text, boolean state, Consumer<Boolean> onClick) {
      this.entries.child((new Checkbox(this, text, state, onClick)).margins(Insets.of(2)));
      return this;
   }

   public DropdownComponent nested(Component text, Sizing horizontalSizing, Consumer<DropdownComponent> builder) {
      DropdownComponent nested = new DropdownComponent(horizontalSizing);
      builder.accept(nested);
      this.entries.child((new NestEntry(this, text, nested)).margins(Insets.of(2)));
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

   protected static void drawIconFromTexture(OwoUIGraphics context, ParentUIComponent dropdown, int y, int u, int v) {
      context.blit(RenderPipelines.GUI_TEXTURED, ICONS_TEXTURE, dropdown.x() + dropdown.width() - ((Insets)dropdown.padding().get()).right() - 10, y, (float)u, (float)v, 9, 9, 32, 32);
   }

   protected static class Divider extends BaseUIComponent implements ResizeableComponent {
      public Divider() {
         this.sizing(Sizing.fixed(1));
      }

      public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
         Insets margins = (Insets)this.margins.get();
         graphics.fill(this.x - margins.left(), this.y - margins.top(), this.x + this.width + margins.right(), this.y + this.height + margins.bottom(), -15592942);
      }

      public void setWidth(int width) {
         this.width = width;
      }
   }

   protected static class NestEntry extends LabelComponent {
      private final DropdownComponent child;

      protected NestEntry(DropdownComponent parentDropdown, Component text, DropdownComponent child) {
         super(text);
         this.child = child;
         this.mouseEnter().subscribe((MouseEnter)() -> {
            child.margins(Insets.top(this.y - parentDropdown.y));
            parentDropdown.queue(() -> {
               parentDropdown.removeChild(child);
               parentDropdown.child(child);
            });
         });
      }

      public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
         super.draw(graphics, mouseX, mouseY, partialTicks, delta);
         DropdownComponent.drawIconFromTexture(graphics, this.parent, this.y, 0, 16);
         this.child.closeWhenNotHovered(!PositionedRectangle.of(this.x, this.y, this.parent.width(), this.height).isInBoundingBox((double)mouseX, (double)mouseY));
      }

      protected int determineHorizontalContentSize(Sizing sizing) {
         return super.determineHorizontalContentSize(sizing) + 17;
      }
   }

   protected static class Button extends LabelComponent implements ResizeableComponent {
      protected final DropdownComponent parentDropdown;
      protected Consumer<DropdownComponent> onClick;

      protected Button(DropdownComponent parentDropdown, Component text, Consumer<DropdownComponent> onClick) {
         super(text);
         this.onClick = onClick;
         this.parentDropdown = parentDropdown;
         this.margins(Insets.vertical(1));
         this.cursorStyle(CursorStyle.HAND);
      }

      public void setWidth(int width) {
         this.width = width;
      }

      public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
         super.onMouseDown(click, doubled);
         this.onClick.accept(this.parentDropdown);
         this.playInteractionSound();
         return true;
      }

      public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
         if (this.isInBoundingBox((double)mouseX, (double)mouseY)) {
            Insets margins = (Insets)this.margins.get();
            graphics.fill(this.x - margins.left(), this.y - margins.top(), this.x + this.width + margins.right(), this.y + this.height + margins.bottom(), 1157627903);
         }

         super.draw(graphics, mouseX, mouseY, partialTicks, delta);
      }

      protected void playInteractionSound() {
         UI.Sounds.playButtonSound();
      }
   }

   protected static class Checkbox extends Button {
      protected boolean state;

      public Checkbox(DropdownComponent parentDropdown, Component text, boolean state, Consumer<Boolean> onClick) {
         super(parentDropdown, text, (dropdownComponent) -> {
         });
         this.state = state;
         this.onClick = (dropdownComponent) -> {
            this.state = !this.state;
            onClick.accept(this.state);
         };
      }

      public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
         super.draw(graphics, mouseX, mouseY, partialTicks, delta);
         DropdownComponent.drawIconFromTexture(graphics, this.parent, this.y, this.state ? 16 : 0, 0);
      }

      protected int determineHorizontalContentSize(Sizing sizing) {
         return super.determineHorizontalContentSize(sizing) + 17;
      }

      protected void playInteractionSound() {
         UI.Sounds.playInteractionSound();
      }
   }

   protected interface ResizeableComponent {
      void setWidth(int var1);
   }
}
