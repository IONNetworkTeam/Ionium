package org.taumc.celeritas.mixin.core;

//? if >=1.8 {
/*import net.minecraft.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.taumc.celeritas.impl.render.text.SignTextCache;
import org.taumc.celeritas.impl.render.text.SignTextCacheHolder;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin implements SignTextCacheHolder {
    @Unique
    private SignTextCache celeritas$signTextCache;

    @Override
    public SignTextCache celeritas$getSignTextCache() {
        SignTextCache cache = this.celeritas$signTextCache;
        if (cache == null) {
            cache = new SignTextCache();
            this.celeritas$signTextCache = cache;
        }
        return cache;
    }
}
*///?}
