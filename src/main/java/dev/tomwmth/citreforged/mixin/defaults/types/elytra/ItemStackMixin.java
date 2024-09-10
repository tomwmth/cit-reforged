package dev.tomwmth.citreforged.mixin.defaults.types.elytra;

import dev.tomwmth.citreforged.cit.CITCache;
import dev.tomwmth.citreforged.defaults.cit.types.TypeElytra;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements TypeElytra.CITCacheElytra {
    @Unique private final CITCache.Single<TypeElytra> citresewn$cacheTypeElytra = new CITCache.Single<>(TypeElytra.CONTAINER::getRealTimeCIT);

    @Override
    public CITCache.Single<TypeElytra> citresewn$getCacheTypeElytra() {
        return this.citresewn$cacheTypeElytra;
    }
}
