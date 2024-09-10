package dev.tomwmth.citreforged.mixin;

import dev.tomwmth.citreforged.cit.ActiveCITs;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Thomas Wearmouth
 * Created on 07/09/2024
 */
@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    @Shadow @Final protected ResourceManager resourceManager;

    /**
     * @see ActiveCITs#load(ResourceManager, ProfilerFiller)
     */
    @Inject(method = "processLoading", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
            ordinal = 0
    ))
    private void citresewn$loadCITs(ProfilerFiller profiler, int maxMipmapLevel, CallbackInfo ci) {
        profiler.push("citresewn:reloading_cits");
        ActiveCITs.load(this.resourceManager, profiler);
        profiler.pop();
    }
}
