package org.taumc.celeritas.mixin.core;

//? if >=1.8 {
/*import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.resource.manager.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.render.text.SignTextCache;

/^* Drops the cached sign text when a font reloads its glyph widths. ^/
@Mixin(TextRenderer.class)
public class TextRendererReloadMixin {
    @Inject(method = "reload", at = @At("RETURN"))
    private void celeritas$invalidateSignText(ResourceManager resourceManager, CallbackInfo ci) {
        SignTextCache.invalidateFonts();
    }
}
*///?}
