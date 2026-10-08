package org.taumc.celeritas.mixin.forge;

//? if >=1.8 {
/*import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/^*
 * Forge 1.8.9 blends the sky colour over the biomes around the player, sampling a square of up to 69x69 columns
 * (53x53 at render distance 12) whenever fancy graphics is on. The result is meant to be cached per column, but the
 * cache stores {@code center.getY()} as the Z coordinate, so it almost never hits and the whole square is resampled
 * several times per frame. This makes the cache store Z, as intended. Only applied on Forge.
 ^/
@Mixin(targets = "net.minecraftforge.client.ForgeHooksClient", remap = false)
public class ForgeHooksClientMixin {
    @Redirect(method = "getSkyBlendColour", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I", remap = true))
    private static int celeritas$cacheZInsteadOfY(BlockPos center) {
        return center.getZ();
    }
}
*///?}
