package dev.tomwmth.citreforged.mixin.defaults.types.armor;

import dev.tomwmth.citreforged.cit.CITCache;
import dev.tomwmth.citreforged.defaults.cit.types.TypeArmor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements TypeArmor.CITCacheArmor {
    @Unique private final CITCache.Single<TypeArmor> citresewn$cacheTypeArmor = new CITCache.Single<>(TypeArmor.CONTAINER::getRealTimeCIT);

    @Override
    public CITCache.Single<TypeArmor> citresewn$getCacheTypeArmor() {
        return this.citresewn$cacheTypeArmor;
    }
}
