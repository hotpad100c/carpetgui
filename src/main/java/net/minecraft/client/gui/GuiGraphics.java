//? if <1.20 {
/*package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import ml.mypals.carpetgui.mixin.ui.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
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
        GuiComponent.fill(this.pose, minX, minY, maxX, maxY, z, color);
    }

    public void fillGradient(int minX, int minY, int maxX, int maxY, int color1, int color2) {
        GuiComponent.fillGradient(this.pose, minX, minY, maxX, maxY, color1, color2);
    }

    public void renderOutline(int x, int y, int width, int height, int color) {
        GuiComponent.renderOutline(this.pose, x, y, width, height, color);
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
        GuiComponent.enableScissor(minX, minY, maxX, maxY);
    }

    public void disableScissor() {
        GuiComponent.disableScissor();
    }

    public void blit(Identifier texture, int x, int y, int u, int v, int width, int height) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(this.pose, x, y, 0, (float)u, (float)v, width, height, 256, 256);
    }

    public void blit(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(this.pose, x, y, 0, u, v, width, height, textureWidth, textureHeight);
    }

    public void renderComponentHoverEffect(Font font, Style style, int x, int y) {
        if (this.minecraft.screen != null) {
            ((ScreenAccessor)this.minecraft.screen).carpetGUI$renderComponentHoverEffect(this.pose, style, x, y);
        }
    }

    private void renderTooltipInternal(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner) {
        if (this.minecraft.screen != null) {
            ((ScreenAccessor)this.minecraft.screen).carpetGUI$renderTooltipInternal(this.pose, components, x, y, positioner);
        }
    }
}
*///?}
