package dev.tomwmth.citreforged.mixin;

import dev.tomwmth.citreforged.cit.ActiveCITs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Thomas Wearmouth
 * Created on 07/09/2024
 */
@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    /**
     * @see ActiveCITs#load(ResourceManager, ProfilerFiller)
     */
    @Redirect(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
            ordinal = 0
    ))
    private void citresewn$loadCITs(ProfilerFiller instance, String s) {
        instance.push("citresewn:reloading_cits");
        ActiveCITs.load(Minecraft.getInstance().getResourceManager(), instance);
        instance.pop();

        instance.push(s);
    }
}
