package dev.tomwmth.citreforged.mixin.defaults.types.item;

import dev.tomwmth.citreforged.defaults.cit.types.TypeItem;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin implements TypeItem.BakedModelManagerMixinAccess {
    @Unique private BakedModel citresewn$forcedMojankModel = null;

    @Inject(method = "getModel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;", cancellable = true, at = @At("HEAD"))
    private void citresewn$getCITMojankModel(ModelResourceLocation id, CallbackInfoReturnable<BakedModel> cir) {
        if (this.citresewn$forcedMojankModel != null) {
            cir.setReturnValue(citresewn$forcedMojankModel);
            this.citresewn$forcedMojankModel = null;
        }
    }

    @Override
    public void citresewn$forceMojankModel(BakedModel model) {
        this.citresewn$forcedMojankModel = model;
    }
}
