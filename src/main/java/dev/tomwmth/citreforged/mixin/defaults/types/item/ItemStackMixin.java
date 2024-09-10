package dev.tomwmth.citreforged.mixin.defaults.types.item;

import dev.tomwmth.citreforged.cit.CITCache;
import dev.tomwmth.citreforged.defaults.cit.types.TypeItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements TypeItem.CITCacheItem {
    @Unique private final CITCache.Single<TypeItem> citresewn$cacheTypeItem = new CITCache.Single<>(TypeItem.CONTAINER::getRealTimeCIT);

    @Override
    public CITCache.Single<TypeItem> citresewn$getCacheTypeItem() {
        return this.citresewn$cacheTypeItem;
    }
}
