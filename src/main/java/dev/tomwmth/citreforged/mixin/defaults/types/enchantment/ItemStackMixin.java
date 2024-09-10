package dev.tomwmth.citreforged.mixin.defaults.types.enchantment;

import dev.tomwmth.citreforged.cit.CITCache;
import dev.tomwmth.citreforged.defaults.cit.types.TypeEnchantment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements TypeEnchantment.CITCacheEnchantment {
    @Unique private final CITCache.MultiList<TypeEnchantment> citresewn$cacheTypeEnchantment = new CITCache.MultiList<>(TypeEnchantment.CONTAINER::getRealTimeCIT);

    @Override
    public CITCache.MultiList<TypeEnchantment> citresewn$getCacheTypeEnchantment() {
        return this.citresewn$cacheTypeEnchantment;
    }

    @Inject(method = "hasFoil", cancellable = true, at = @At("HEAD"))
    private void citresewn$enchantment$disableDefaultGlint(CallbackInfoReturnable<Boolean> cir) {
        if (TypeEnchantment.CONTAINER.shouldNotApplyDefaultGlint())
            cir.setReturnValue(false);
    }
}
