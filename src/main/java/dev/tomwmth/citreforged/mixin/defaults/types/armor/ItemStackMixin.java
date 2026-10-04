package dev.tomwmth.citreforged.mixin.defaults.types.armor;

import dev.tomwmth.citreforged.cit.CITCache;
import dev.tomwmth.citreforged.defaults.cit.types.TypeArmor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements TypeArmor.CITCacheArmor {
    @Unique private volatile CITCache.Single<TypeArmor> citresewn$cacheTypeArmor;

    @Override
    public CITCache.Single<TypeArmor> citresewn$getCacheTypeArmor() {
        CITCache.Single<TypeArmor> cache = this.citresewn$cacheTypeArmor;
        if (cache == null) {
            synchronized (this) {
                cache = this.citresewn$cacheTypeArmor;
                if (cache == null) {
                    cache = new CITCache.Single<>(TypeArmor.CONTAINER::getRealTimeCIT);
                    this.citresewn$cacheTypeArmor = cache;
                }
            }
        }
        return cache;
    }
}
