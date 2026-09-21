package ml.mypals.carpetgui.mixin.compat;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkMap.class, priority = 5000)
public abstract class ChunkMapRecursionFixMixin {
    @Shadow
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap;

    @Inject(method = "getChunks", at = @At("HEAD"), cancellable = true, require = 0)
    private void carpetGUI$breakRecursion(CallbackInfoReturnable<Iterable<ChunkHolder>> cir) {
        if (this.visibleChunkMap != null) {
            cir.setReturnValue(Iterables.unmodifiableIterable(this.visibleChunkMap.values()));
        }
    }
}
