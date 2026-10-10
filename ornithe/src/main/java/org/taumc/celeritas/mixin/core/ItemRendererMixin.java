package org.taumc.celeritas.mixin.core;

//? if >=1.8 {
/*import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.render.entity.ItemFrameCompass;

/^*
 * Lets {@link ItemFrameCompass} swap the texture while the quads of a compass in an item frame are drawn. The
 * enchantment glint that may follow is drawn from the atlas again, as in vanilla.
 ^/
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;render(Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/item/ItemStack;)V"))
    private void celeritas$beginCompassModel(ItemStack stack, BakedModel model, CallbackInfo ci) {
        ItemFrameCompass.beginModel(model);
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;render(Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void celeritas$endCompassModel(ItemStack stack, BakedModel model, CallbackInfo ci) {
        ItemFrameCompass.endModel();
    }
}
*///?}
