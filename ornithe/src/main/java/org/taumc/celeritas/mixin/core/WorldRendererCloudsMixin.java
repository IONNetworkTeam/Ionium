package org.taumc.celeritas.mixin.core;

//? if >=1.8 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.render.clouds.FancyCloudRenderer;

/^*
 * Replaces the fancy clouds, which vanilla rebuilds vertex by vertex every frame, with cached geometry. Custom cloud
 * renderers that Forge dimensions register are called before this method and are not affected.
 ^/
@Mixin(WorldRenderer.class)
public class WorldRendererCloudsMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private ClientWorld world;

    @Shadow
    private int ticks;

    @Inject(method = "renderFancyClouds", at = @At("HEAD"), cancellable = true)
    private void celeritas$renderCachedClouds(float tickDelta, int anaglyphPass, CallbackInfo ci) {
        FancyCloudRenderer.render(this.minecraft, this.world, this.ticks, tickDelta, anaglyphPass);
        ci.cancel();
    }
}
*///?}
