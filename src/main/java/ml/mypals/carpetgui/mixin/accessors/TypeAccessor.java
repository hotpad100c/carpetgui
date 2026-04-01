package ml.mypals.carpetgui.mixin.accessors;
//? if <1.21.11 {

/*import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(GameRules.Value.class)
public interface TypeAccessor {
    @Accessor("type")
    GameRules.Type<?> carpetGUI$getType();
}
*///?} else {
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(Minecraft.class)
public interface TypeAccessor {
}
//?}