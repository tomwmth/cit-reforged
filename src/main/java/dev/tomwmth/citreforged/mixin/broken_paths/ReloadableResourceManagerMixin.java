package dev.tomwmth.citreforged.mixin.broken_paths;

import dev.tomwmth.citreforged.CITReforged;
import dev.tomwmth.citreforged.config.BrokenPaths;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Starts/Stops broken paths logic.
 * @see BrokenPaths
 * @see ResourceLocationMixin
 */
@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerMixin {
    @Shadow @Final private PackType type;

    @Inject(method = "createReload", at = @At("RETURN"))
    public void citresewn$brokenpaths$onReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<PackResources> packs, CallbackInfoReturnable<ReloadInstance> cir) {
        if (BrokenPaths.processingBrokenPaths = this.type == PackType.CLIENT_RESOURCES) {
            CITReforged.LOGGER.info("Caution! Broken paths is enabled!");
            cir.getReturnValue().done().thenRun(() -> BrokenPaths.processingBrokenPaths = false);
        }
    }
}
