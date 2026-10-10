package org.taumc.celeritas.mixin.core;

//? if >=1.8 {
/*import net.minecraft.client.render.entity.ItemFrameRenderer;
import net.minecraft.client.render.texture.CompassSprite;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.render.entity.ItemFrameCompass;

/^*
 * Keeps compasses in item frames from rewriting the block atlas twice per item frame per frame
 * (see {@link ItemFrameCompass}). The second tick, which animates the compass of the player, is left alone.
 ^/
@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererMixin {
    @Redirect(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/texture/CompassSprite;tick(Lnet/minecraft/world/World;DDDZZ)V"))
    private void celeritas$prepareCompass(CompassSprite sprite, World world, double x, double z, double yaw, boolean findSpawnPoint, boolean skipAngleInterpolation) {
        if (!ItemFrameCompass.prepare(sprite, world, x, z, yaw)) {
            sprite.tick(world, x, z, yaw, findSpawnPoint, skipAngleInterpolation);
        }
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void celeritas$finishCompass(ItemFrameEntity itemFrame, CallbackInfo ci) {
        ItemFrameCompass.reset();
    }
}
*///?}
