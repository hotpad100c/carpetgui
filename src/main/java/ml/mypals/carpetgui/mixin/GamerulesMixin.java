package ml.mypals.carpetgui.mixin;

//? if <1.21.11 {

/*import ml.mypals.carpetgui.settings.GamerulesDefaultValueSorter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRules.class)
public class GamerulesMixin {
    @Unique
    private static GameRules.GameRuleTypeVisitor DETECTOR;

    @Inject(method = "register", at = @At("RETURN"))
    private static <T extends GameRules.Value<T>> void register(String string, GameRules.Category category, GameRules.Type<T> type, CallbackInfoReturnable<GameRules.Key<T>> cir) {
        if (DETECTOR == null) {
            DETECTOR = new GameRules.GameRuleTypeVisitor() {
                @Override
                public <U extends GameRules.Value<U>> void visit(GameRules.@NotNull Key<U> key, GameRules.@NotNull Type<U> type) {
                    GameRules.GameRuleTypeVisitor.super.visit(key, type);
                }

                @Override
                public void visitBoolean(GameRules.@NotNull Key<GameRules.BooleanValue> key, GameRules.@NotNull Type<GameRules.BooleanValue> type) {
                    GamerulesDefaultValueSorter.gamerulesDefaultValues.put(
                            key,
                            type.createRule().get() ? "true" : "false"
                    );
                    GameRules.GameRuleTypeVisitor.super.visitBoolean(key, type);
                }

                @Override
                public void visitInteger(GameRules.@NotNull Key<GameRules.IntegerValue> key, GameRules.@NotNull Type<GameRules.IntegerValue> type) {
                    GamerulesDefaultValueSorter.gamerulesDefaultValues.put(
                            key,
                            type.createRule().serialize()
                    );
                    GameRules.GameRuleTypeVisitor.super.visitInteger(key, type);
                }
            };
        }
        type.callVisitor(DETECTOR, cir.getReturnValue());
    }
}
*///?} else {
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(Minecraft.class)
public class GamerulesMixin {
}
//?}
