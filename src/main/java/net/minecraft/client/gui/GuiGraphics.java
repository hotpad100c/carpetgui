//? if <1.20 {
/*package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

import com.mojang.blaze3d.vertex.Tesselator;
import ml.mypals.carpetgui.mixin.ui.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//? if >=1.19.3 {
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
//?}
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
        this.pose.pushPose();
        this.pose.translate(0, 0, z);
        GuiComponent.fill(this.pose, minX, minY, maxX, maxY, color);
        this.pose.popPose();
    }

    public void fillGradient(int minX, int minY, int maxX, int maxY, int color1, int color2) {
        //? if <=1.16.5 {

        /^RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);

        GuiComponent.fillGradient(this.pose.last().pose(), bufferBuilder ,minX, minY, maxX, maxY, color1, color2, 0);

        tesselator.end();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
        ^///?} else {
        GuiComponent.fillGradient(this.pose, minX, minY, maxX, maxY, color1, color2, 0);
        //?}
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

    //? if <1.19 {
    /^private static final java.util.Deque<int[]> SCISSOR_STACK = new java.util.ArrayDeque<>();

    private void applyScissor(int[] rect) {
        if (rect == null) {
            RenderSystem.disableScissor();
            return;
        }
        com.mojang.blaze3d.platform.Window window = this.minecraft.getWindow();
        double scale = window.getGuiScale();
        int frameH = window.getHeight();
        int x = Math.max(0, (int)(rect[0] * scale));
        int y = Math.max(0, (int)(frameH - rect[3] * scale));
        int width = Math.max(0, (int)((rect[2] - rect[0]) * scale));
        int height = Math.max(0, (int)((rect[3] - rect[1]) * scale));
        RenderSystem.enableScissor(x, y, width, height);
    }
    ^///?}

    public void enableScissor(int minX, int minY, int maxX, int maxY) {
        //? if >=1.19 {
        GuiComponent.enableScissor(minX, minY, maxX, maxY);
        //?} else {
        /^int[] parent = SCISSOR_STACK.peek();
        int[] next;
        if (parent != null) {
            int x1 = Math.max(parent[0], minX);
            int y1 = Math.max(parent[1], minY);
            int x2 = Math.min(parent[2], maxX);
            int y2 = Math.min(parent[3], maxY);
            if (x2 < x1) x2 = x1;
            if (y2 < y1) y2 = y1;
            next = new int[]{x1, y1, x2, y2};
        } else {
            next = new int[]{minX, minY, Math.max(minX, maxX), Math.max(minY, maxY)};
        }
        SCISSOR_STACK.push(next);
        this.applyScissor(next);
        ^///?}
    }

    public void disableScissor() {
        //? if >=1.19 {
        GuiComponent.disableScissor();
        //?} else {
        /^if (!SCISSOR_STACK.isEmpty()) {
            SCISSOR_STACK.pop();
        }
        this.applyScissor(SCISSOR_STACK.peek());
        ^///?}
    }

    public void blit(Identifier texture, int x, int y, int u, int v, int width, int height) {
        //? if <1.17 {
        /^this.minecraft.getTextureManager().bind(texture);
        ^///?} else {
        RenderSystem.setShaderTexture(0, texture);
        //?}
        GuiComponent.blit(this.pose, x, y, 0, (float)u, (float)v, width, height, 256, 256);
    }

    public void blit(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        //? if <1.17 {
        /^this.minecraft.getTextureManager().bind(texture);
        ^///?} else {
        RenderSystem.setShaderTexture(0, texture);
        //?}
        GuiComponent.blit(this.pose, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public void blit(Identifier texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        //? if <1.17 {
        /^this.minecraft.getTextureManager().bind(texture);
        ^///?} else {
        RenderSystem.setShaderTexture(0, texture);
        //?}
        GuiComponent.blit(this.pose, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
    }

    public void renderComponentHoverEffect(Font font, Style style, int x, int y) {
        if (this.minecraft.screen != null) {
            ((ScreenAccessor)this.minecraft.screen).carpetGUI$renderComponentHoverEffect(this.pose, style, x, y);
        }
    }

    //? if <1.17 {
    /^public void renderTooltip(Font font, List<ClientTooltipComponent> components, int x, int y) {
        if (this.minecraft.screen != null) {
            List<FormattedCharSequence> lines = new java.util.ArrayList<>();
            for (ClientTooltipComponent comp : components) {
                if (comp != null && comp.getText() != null) {
                    lines.add(comp.getText());
                }
            }
            this.minecraft.screen.renderTooltip(this.pose, lines, x, y);
        }
    }
    ^///?} elif <1.19.3 {
    /^public void renderTooltip(Font font, List<ClientTooltipComponent> components, int x, int y) {
        if (this.minecraft.screen != null) {
            ((ScreenAccessor)this.minecraft.screen).carpetGUI$renderTooltipInternal(this.pose, components, x, y);
        }
    }
    ^///?} else {
    public void renderTooltip(Font font, List<ClientTooltipComponent> components, int x, int y) {
        this.renderTooltip(font, components, x, y, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE);
    }
    public void renderTooltip(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner) {
        if (this.minecraft.screen != null) {
            ((ScreenAccessor)this.minecraft.screen).carpetGUI$renderTooltipInternal(this.pose, components, x, y, positioner);
        }
    }
    //?}
}
*///?}
