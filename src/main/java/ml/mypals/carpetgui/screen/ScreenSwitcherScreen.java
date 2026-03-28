package ml.mypals.carpetgui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
@Environment(EnvType.CLIENT)
public class ScreenSwitcherScreen extends Screen {

    static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("gamemode_switcher/slot");
    static final ResourceLocation SELECTION_SPRITE = ResourceLocation.withDefaultNamespace("gamemode_switcher/selection");

    private static final int SLOT_AREA        = 26;
    private static final int SLOT_PADDING     = 5;
    private static final int SLOT_AREA_PADDED = 31;

    private static final int ICON_OFFSET = 5;

    private static final List<ScreenEntry> ENTRIES = new ArrayList<>();

    private static KeyMapping triggerKey;


    public static void registerEntry(Component name, ItemStack icon, Runnable factory) {
        ENTRIES.add(new ScreenEntry(ENTRIES.size(), name, icon, factory));
    }

    public static void setTriggerKey(KeyMapping key) {
        triggerKey = key;
    }

    private final int allSlotsWidth;

    private ScreenEntry currentlyHovered;

    private int  firstMouseX, firstMouseY;
    private boolean setFirstMousePos = false;

    private final List<ScreenSlot> slots = Lists.newArrayList();

    public ScreenSwitcherScreen() {
        super(net.minecraft.client.GameNarrator.NO_TITLE);
        this.allSlotsWidth   = ENTRIES.isEmpty() ? 0 : ENTRIES.size() * SLOT_AREA_PADDED - SLOT_PADDING;
        this.currentlyHovered = ENTRIES.isEmpty() ? null : ENTRIES.get(0);
    }

    @Override
    protected void init() {
        super.init();
        this.slots.clear();

        int startX = this.width  / 2 - this.allSlotsWidth / 2;
        int slotY  = this.height / 2 - SLOT_AREA;

        for (ScreenEntry entry : ENTRIES) {
            int x = startX + entry.index() * SLOT_AREA_PADDED;
            this.slots.add(new ScreenSlot(entry, x, slotY));
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (this.checkToClose()) return;

        super.render(guiGraphics, mouseX, mouseY, delta);

        if (this.currentlyHovered != null) {
            guiGraphics.drawCenteredString(
                    this.font,
                    this.currentlyHovered.name(),
                    this.width  / 2,
                    this.height / 2 - SLOT_AREA - 14,
                    0xFFFFFF);
        }

        if (!this.setFirstMousePos) {
            this.firstMouseX    = mouseX;
            this.firstMouseY    = mouseY;
            this.setFirstMousePos = true;
        }
        boolean mouseSteady = (this.firstMouseX == mouseX && this.firstMouseY == mouseY);

        for (ScreenSlot slot : this.slots) {
            slot.render(guiGraphics, mouseX, mouseY, delta);
            slot.setSelected(this.currentlyHovered == slot.entry);
            if (!mouseSteady && slot.isHoveredOrFocused()) {
                this.currentlyHovered = slot.entry;
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean checkToClose() {
        if(minecraft == null) return false;
        boolean keyStillHeld = triggerKey != null && InputConstants.isKeyDown(
                this.minecraft.getWindow().getWindow(),
                triggerKey.getDefaultKey().getValue());

        if (!keyStillHeld) {
            this.minecraft.setScreen(null);
            openSelected();
            return true;
        }
        return false;
    }

    private void openSelected() {
        if (this.currentlyHovered == null) return;
        this.currentlyHovered.factory().run();
    }

    public record ScreenEntry(
            int index,
            Component name,
            ItemStack icon,
            Runnable factory
    ) {}

    @Environment(EnvType.CLIENT)
    public static class ScreenSlot extends AbstractWidget {

        final ScreenEntry entry;
        private boolean isSelected;

        public ScreenSlot(ScreenEntry entry, int x, int y) {
            super(x, y, SLOT_AREA, SLOT_AREA, entry.name());
            this.entry = entry;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            guiGraphics.blitSprite(SLOT_SPRITE, this.getX(), this.getY(), SLOT_AREA, SLOT_AREA);
            guiGraphics.renderItem(this.entry.icon(), this.getX() + ICON_OFFSET, this.getY() + ICON_OFFSET);
            if (this.isSelected) {
                guiGraphics.blitSprite(SELECTION_SPRITE, this.getX(), this.getY(), SLOT_AREA, SLOT_AREA);
            }
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
        @Override
        public boolean isHoveredOrFocused() {
            return super.isHoveredOrFocused() || this.isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
        }
    }
}