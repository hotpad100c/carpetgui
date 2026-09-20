//? if <1.20 {
package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import ml.mypals.carpetgui.mixin.ui.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//? if >=1.19.3 {
/*import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
*///?}
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class GuiGraphics {
    private final PoseStack pose;
    private final Minecraft minecraft;
    private final MultiBufferSource.BufferSource bufferSource;

    public GuiGraphics(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource) {
        this(minecraft, new PoseStack(), bufferSource);
    }

    public GuiGraphics(Minecraft minecraft, PoseStack pose, MultiBufferSource.BufferSource bufferSource) {
        this.minecraft = minecraft;
        this.pose = pose;
        this.bufferSource = bufferSource;
    }

    public PoseStack pose() {
        return this.pose;
    }

    public void fill(int minX, int minY, int maxX, int maxY, int color) {
        GuiComponent.fill(this.pose, minX, minY, maxX, maxY, color);
    }

    public void fill(int minX, int minY, int maxX, int maxY, int z, int color) {
        this.pose.pushPose();
        this.pose.translate(0, 0, z);
        GuiComponent.fill(this.pose, minX, minY, maxX, maxY, color);
        this.pose.popPose();
    }

    public void fillGradient(int minX, int minY, int maxX, int maxY, int color1, int color2) {
        GuiComponent.fillGradient(this.pose, minX, minY, maxX, maxY, color1, color2, 0);
    }

    public void renderOutline(int x, int y, int width, int height, int color) {
        this.fill(x, y, x + width, y + 1, color);
        this.fill(x, y + height - 1, x + width, y + height, color);
        this.fill(x, y + 1, x + 1, y + height - 1, color);
        this.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        GuiComponent.drawCenteredString(this.pose, font, text, x, y, color);
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        GuiComponent.drawCenteredString(this.pose, font, text, x, y, color);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(this.pose, text, (float)x, (float)y, color) : font.draw(this.pose, text, (float)x, (float)y, color);
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(this.pose, text, (float)x, (float)y, color) : font.draw(this.pose, text, (float)x, (float)y, color);
    }

    public int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(this.pose, text, (float)x, (float)y, color) : font.draw(this.pose, text, (float)x, (float)y, color);
    }

    public void enableScissor(int minX, int minY, int maxX, int maxY) {
        //? if >=1.19 {
        /*GuiComponent.enableScissor(minX, minY, maxX, maxY);
        *///?} else {
        com.mojang.blaze3d.platform.Window window = this.minecraft.getWindow();
        double scale = window.getGuiScale();
        int frameH = window.getHeight();
        int x = Math.max(0, (int)(minX * scale));
        int y = Math.max(0, (int)(frameH - maxY * scale));
        int width = Math.max(0, (int)((maxX - minX) * scale));
        int height = Math.max(0, (int)((maxY - minY) * scale));
        RenderSystem.enableScissor(x, y, width, height);
        //?}
    }

    public void disableScissor() {
        //? if >=1.19 {
        /*GuiComponent.disableScissor();
        *///?} else {
        RenderSystem.disableScissor();
        //?}
    }

    public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(this.pose, x, y, 0, (float)u, (float)v, width, height, 256, 256);
    }

    public void blit(ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(this.pose, x, y, 0, u, v, width, height, textureWidth, textureHeight);
    }

    public void renderComponentHoverEffect(Font font, Style style, int x, int y) {
        if (this.minecraft.screen != null) {
            ((ScreenAccessor)this.minecraft.screen).carpetGUI$renderComponentHoverEffect(this.pose, style, x, y);
        }
    }

    //? if <1.19.3 {
    public void renderTooltip(Font font, List<ClientTooltipComponent> components, int x, int y) {
        if (this.minecraft.screen != null) {
            ((ScreenAccessor)this.minecraft.screen).carpetGUI$renderTooltipInternal(this.pose, components, x, y);
        }
    }
    //?} else {
    /*public void renderTooltip(Font font, List<ClientTooltipComponent> components, int x, int y) {
        this.renderTooltip(font, components, x, y, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE);
    }
    public void renderTooltip(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner) {
        if (this.minecraft.screen != null) {
            ((ScreenAccessor)this.minecraft.screen).carpetGUI$renderTooltipInternal(this.pose, components, x, y, positioner);
        }
    }
    *///?}
}
//?}
