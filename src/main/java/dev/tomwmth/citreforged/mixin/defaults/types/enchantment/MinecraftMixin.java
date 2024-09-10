package dev.tomwmth.citreforged.mixin.defaults.types.enchantment;

import dev.tomwmth.citreforged.defaults.cit.types.TypeEnchantment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        TypeEnchantment.MergeMethodIntensity.MergeMethod.ticks++;
    }
}
