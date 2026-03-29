package ml.mypals.carpetgui.mixin.accessors;

import ml.mypals.carpetgui.accessors.CommandSourceStackAccessor;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.*;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackMixin implements CommandSourceStackAccessor {

    @Shadow
    @Final
    @Mutable
    private boolean silent;

    @Override
    public void carpetGUI$setSilent(boolean silent) {
        this.silent = silent;
    }
}