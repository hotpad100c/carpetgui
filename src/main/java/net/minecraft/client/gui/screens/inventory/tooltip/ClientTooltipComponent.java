//? if <1.17 {
package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.util.FormattedCharSequence;

public interface ClientTooltipComponent {
    static ClientTooltipComponent create(FormattedCharSequence text) {
        return () -> text;
    }

    FormattedCharSequence getText();
}
//?}
