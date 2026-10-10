package org.taumc.celeritas.mixin.core;

//? if >=1.8 {
/*import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.block.entity.SignRenderer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.taumc.celeritas.impl.render.text.SignTextCacheHolder;

import java.util.List;

/^*
 * Reuses the wrapped and formatted sign lines between frames (see {@link org.taumc.celeritas.impl.render.text.SignTextCache}).
 ^/
@Mixin(SignRenderer.class)
public class SignRendererMixin {
    private static final String RENDER = "render(Lnet/minecraft/block/entity/SignBlockEntity;DDDFI)V";

    @Redirect(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderUtils;wrapText(Lnet/minecraft/text/Text;ILnet/minecraft/client/render/TextRenderer;ZZ)Ljava/util/List;"))
    private List<Text> celeritas$wrapCached(Text text, int width, TextRenderer font, boolean stripLeadingSpaces, boolean allowFormatting, SignBlockEntity sign) {
        return ((SignTextCacheHolder) sign).celeritas$getSignTextCache().wrap(text, width, font, stripLeadingSpaces, allowFormatting);
    }

    @Redirect(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;getFormattedString()Ljava/lang/String;"))
    private String celeritas$formattedCached(Text first, SignBlockEntity sign) {
        return ((SignTextCacheHolder) sign).celeritas$getSignTextCache().formatted(first);
    }

    @Redirect(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;getWidth(Ljava/lang/String;)I"))
    private int celeritas$widthCached(TextRenderer font, String string, SignBlockEntity sign) {
        return ((SignTextCacheHolder) sign).celeritas$getSignTextCache().width(string, font);
    }
}
*///?}
