package dev.tomwmth.citreforged.mixin.defaults.types.item;

import dev.tomwmth.citreforged.cit.CITCache;
import dev.tomwmth.citreforged.defaults.cit.types.TypeItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements TypeItem.CITCacheItem {
    @Unique private volatile CITCache.Single<TypeItem> citresewn$cacheTypeItem;

    @Override
    public CITCache.Single<TypeItem> citresewn$getCacheTypeItem() {
        CITCache.Single<TypeItem> cache = this.citresewn$cacheTypeItem;
        if (cache == null) {
            synchronized (this) {
                cache = this.citresewn$cacheTypeItem;
                if (cache == null) {
                    cache = new CITCache.Single<>(TypeItem.CONTAINER::getRealTimeCIT);
                    this.citresewn$cacheTypeItem = cache;
                }
            }
        }
        return cache;
    }
}
