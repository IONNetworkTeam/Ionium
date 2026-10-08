package org.taumc.celeritas.mixin.core;

//? if >=1.8 {
/*import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.render.terrain.fog.GLStateManagerFogService;

/^*
 * Copies every fog change that goes through GlStateManager into GLStateManagerFogService, so the chunk renderer can
 * read the fog state without glGet. The setters skip values that are already set, so storing the argument at HEAD
 * always matches what GlStateManager has cached.
 ^/
@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
    @Inject(method = "enableFog", at = @At("HEAD"))
    private static void celeritas$trackEnableFog(CallbackInfo ci) {
        GLStateManagerFogService.fogEnabled = true;
    }

    @Inject(method = "disableFog", at = @At("HEAD"))
    private static void celeritas$trackDisableFog(CallbackInfo ci) {
        GLStateManagerFogService.fogEnabled = false;
    }

    @Inject(method = "fogMode", at = @At("HEAD"))
    private static void celeritas$trackFogMode(int mode, CallbackInfo ci) {
        GLStateManagerFogService.fogMode = mode;
    }

    @Inject(method = "fogDensity", at = @At("HEAD"))
    private static void celeritas$trackFogDensity(float density, CallbackInfo ci) {
        GLStateManagerFogService.fogDensity = density;
    }

    @Inject(method = "fogStart", at = @At("HEAD"))
    private static void celeritas$trackFogStart(float start, CallbackInfo ci) {
        GLStateManagerFogService.fogStart = start;
    }

    @Inject(method = "fogEnd", at = @At("HEAD"))
    private static void celeritas$trackFogEnd(float end, CallbackInfo ci) {
        GLStateManagerFogService.fogEnd = end;
    }
}
*///?}
