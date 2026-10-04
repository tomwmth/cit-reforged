package dev.tomwmth.citreforged.mixin.defaults.types.elytra;

import dev.tomwmth.citreforged.cit.CITCache;
import dev.tomwmth.citreforged.defaults.cit.types.TypeElytra;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements TypeElytra.CITCacheElytra {
    @Unique private volatile CITCache.Single<TypeElytra> citresewn$cacheTypeElytra;

    @Override
    public CITCache.Single<TypeElytra> citresewn$getCacheTypeElytra() {
        CITCache.Single<TypeElytra> cache = this.citresewn$cacheTypeElytra;
        if (cache == null) {
            synchronized (this) {
                cache = this.citresewn$cacheTypeElytra;
                if (cache == null) {
                    cache = new CITCache.Single<>(TypeElytra.CONTAINER::getRealTimeCIT);
                    this.citresewn$cacheTypeElytra = cache;
                }
            }
        }
        return cache;
    }
}
