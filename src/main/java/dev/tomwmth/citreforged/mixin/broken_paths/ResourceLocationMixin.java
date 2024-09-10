package dev.tomwmth.citreforged.mixin.broken_paths;

import dev.tomwmth.citreforged.config.BrokenPaths;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies broken paths logic when active.
 * @see BrokenPaths
 * @see ReloadableResourceManagerMixin
 */
@Mixin(ResourceLocation.class)
public abstract class ResourceLocationMixin {
    @Inject(method = "isValidPath", cancellable = true, at = @At("RETURN"))
    private static void citresewn$brokenpaths$processBrokenPaths(String path, CallbackInfoReturnable<Boolean> cir) {
        if (!BrokenPaths.processingBrokenPaths)
            return;

        if (!cir.getReturnValue()) {
            cir.setReturnValue(true);
        }
    }
}
