package org.taumc.celeritas.mixin.forge;

//? if >=1.8 {
/*import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.render.text.GlyphBatchHolder;

/^*
 * The Forge side of the glyph batching (see {@code core.TextRendererMixin}). Forge 1.8.9 routes the colour of the
 * text through {@code setColor}, the texture binds through {@code bindTexture} and draws the underline and
 * strikethrough quads in {@code doDraw}; the hooks keep their names at runtime, so this only loads on Forge.
 ^/
@Mixin(TextRenderer.class)
public class ForgeTextRendererMixin {
    @Shadow
    @Final
    private TextureManager textureManager;

    // the hook does not exist in the Ornithe jar, so the remappers cannot resolve it as a string; class literals
    // are remapped like any other class reference
    @Redirect(method = { "drawBasicGlyph", "bindFontPageTexture" }, at = @At(value = "INVOKE", desc = @Desc(owner = TextRenderer.class, value = "bindTexture", args = Identifier.class)))
    private void celeritas$bindFontTexture(TextRenderer self, Identifier identifier) {
        ((GlyphBatchHolder) this).celeritas$getGlyphBatch().bindTexture(this.textureManager, identifier);
    }

    @Inject(method = "setColor(FFFF)V", at = @At("HEAD"), remap = false)
    private void celeritas$trackColor(float r, float g, float b, float a, CallbackInfo ci) {
        ((GlyphBatchHolder) this).celeritas$getGlyphBatch().setColor(r, g, b, a);
    }

    @Inject(method = "doDraw(F)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;disableTexture()V"))
    private void celeritas$flushBeforeDecorations(float width, CallbackInfo ci) {
        ((GlyphBatchHolder) this).celeritas$getGlyphBatch().flush();
    }
}
*///?}
