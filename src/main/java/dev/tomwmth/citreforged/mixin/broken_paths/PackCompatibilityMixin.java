package dev.tomwmth.citreforged.mixin.broken_paths;

import dev.tomwmth.citreforged.config.BrokenPaths;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.InclusiveRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a resourcepack compatibility error message when broken paths are enabled and are detected in a pack.
 * @see BrokenPaths
 * @see AbstractPackResourcesMixin
 */
@Mixin(PackCompatibility.class)
public abstract class PackCompatibilityMixin {
    @Unique private static final PackCompatibility BROKEN_PATHS = PackCompatibility("BROKEN_PATHS", -1, "broken_paths");

    @SuppressWarnings("InvokerTarget")
    @Invoker("<init>")
    public static PackCompatibility PackCompatibility(String internalName, int internalId, String translationSuffix) {
        throw new AssertionError();
    }

    @Inject(method = "forVersion", cancellable = true, at = @At("HEAD"))
    private static void citresewn$brokenpaths$redirectBrokenPathsCompatibility(InclusiveRange<Integer> pRange, int current, CallbackInfoReturnable<PackCompatibility> cir) {
        if (current == Integer.MAX_VALUE - 53)
            cir.setReturnValue(BROKEN_PATHS);
    }
}
