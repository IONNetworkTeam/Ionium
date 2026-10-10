package org.taumc.celeritas.mixin.core;

//? if >=1.8 {
/*import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.text.GlyphBatch;
import org.taumc.celeritas.impl.render.text.GlyphBatchHolder;

/^*
 * Batches the glyphs of each text layer into one draw call (see {@link GlyphBatch}). The vanilla methods keep
 * running as they are, including the texture binds and the glColor calls that other code may rely on; only the
 * immediate-mode calls inside the two glyph methods are redirected into the batch.
 *
 * Forge 1.8.9 patches this class: the colour calls go through a setColor hook and the underline and strikethrough
 * quads live in a doDraw hook. The two injections here that depend on the vanilla layout are optional, and
 * {@code forge.ForgeTextRendererMixin} covers the Forge hooks instead.
 ^/
@Mixin(TextRenderer.class)
public class TextRendererMixin implements GlyphBatchHolder {
    @Unique
    private final GlyphBatch celeritas$glyphs = new GlyphBatch();

    @Override
    public GlyphBatch celeritas$getGlyphBatch() {
        return this.celeritas$glyphs;
    }

    @Inject(method = "drawLayer(Ljava/lang/String;Z)V", at = @At("HEAD"))
    private void celeritas$beginLayer(String text, boolean shadow, CallbackInfo ci) {
        this.celeritas$glyphs.begin();
    }

    @Inject(method = "drawLayer(Ljava/lang/String;Z)V", at = @At("RETURN"))
    private void celeritas$endLayer(String text, boolean shadow, CallbackInfo ci) {
        this.celeritas$glyphs.flush();
    }

    // The underline and strikethrough quads are drawn untextured through the shared tessellator; draw the glyphs
    // before them so nothing changes in the draw order.
    @Inject(method = "drawLayer(Ljava/lang/String;Z)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;disableTexture()V"), require = 0, expect = 0)
    private void celeritas$flushBeforeDecorations(String text, boolean shadow, CallbackInfo ci) {
        this.celeritas$glyphs.flush();
    }

    @Redirect(method = { "drawLayer(Ljava/lang/String;Z)V", "drawLayer(Ljava/lang/String;FFIZ)I" },
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;color4f(FFFF)V"), require = 0, expect = 0)
    private void celeritas$trackColor(float r, float g, float b, float a) {
        GlStateManager.color4f(r, g, b, a);
        this.celeritas$glyphs.setColor(r, g, b, a);
    }

    @Inject(method = "drawBasicGlyph", at = @At("HEAD"))
    private void celeritas$selectDefaultFont(int index, boolean italic, CallbackInfoReturnable<Float> cir) {
        this.celeritas$glyphs.selectTexture(GlyphBatch.DEFAULT_FONT);
    }

    @Inject(method = "drawUnicodeGlyph", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;bindFontPageTexture(I)V"))
    private void celeritas$selectUnicodePage(char chr, boolean italic, CallbackInfoReturnable<Float> cir) {
        this.celeritas$glyphs.selectTexture(chr / 256);
    }

    // Vanilla binds the font texture for every glyph; look it up once per layer instead (Forge routes the bind
    // through a hook of its own, see the Forge mixin)
    @Redirect(method = { "drawBasicGlyph", "bindFontPageTexture" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/texture/TextureManager;bind(Lnet/minecraft/resource/Identifier;)V"), require = 0, expect = 0)
    private void celeritas$bindFontTexture(TextureManager textureManager, Identifier identifier) {
        this.celeritas$glyphs.bindTexture(textureManager, identifier);
    }

    @Redirect(method = { "drawBasicGlyph", "drawUnicodeGlyph" }, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBegin(I)V"))
    private void celeritas$beginGlyph(int mode) {
        this.celeritas$glyphs.beginGlyph();
    }

    @Redirect(method = { "drawBasicGlyph", "drawUnicodeGlyph" }, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTexCoord2f(FF)V"))
    private void celeritas$glyphTexCoord(float u, float v) {
        this.celeritas$glyphs.texCoord(u, v);
    }

    @Redirect(method = { "drawBasicGlyph", "drawUnicodeGlyph" }, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glVertex3f(FFF)V"))
    private void celeritas$glyphVertex(float x, float y, float z) {
        this.celeritas$glyphs.vertex(x, y, z);
    }

    @Redirect(method = { "drawBasicGlyph", "drawUnicodeGlyph" }, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEnd()V"))
    private void celeritas$endGlyph() {
        this.celeritas$glyphs.endGlyph();
    }
}
*///?}
