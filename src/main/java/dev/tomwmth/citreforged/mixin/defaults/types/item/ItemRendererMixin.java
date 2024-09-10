package dev.tomwmth.citreforged.mixin.defaults.types.item;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tomwmth.citreforged.cit.CIT;
import dev.tomwmth.citreforged.cit.CITContext;
import dev.tomwmth.citreforged.defaults.cit.types.TypeItem;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;

import static dev.tomwmth.citreforged.defaults.cit.types.TypeItem.CONTAINER;

/**
 * Do not go through this class, it looks awful because it was ported from a "proof of concept".<br>
 * The whole type will be rewritten at some point.
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow @Final private ItemModelShaper itemModelShaper;

    @Unique private WeakReference<BakedModel> citresewn$mojankCITModel = null;

    @Inject(method = "getModel", cancellable = true, at = @At("HEAD"))
    private void citresewn$getItemModel(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (!CONTAINER.active())
            return;

        CITContext context = new CITContext(stack, world, entity);
        CIT<TypeItem> cit = CONTAINER.getCIT(context, seed);
        this.citresewn$mojankCITModel = null;
        if (cit != null) {
            BakedModel citModel = cit.type.getItemModel(context, seed);

            if (citModel != null) {
                if (stack.is(Items.TRIDENT) || stack.is(Items.SPYGLASS)) {
                    this.citresewn$mojankCITModel = new WeakReference<>(citModel);
                } else
                    cir.setReturnValue(citModel);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void citresewn$fixMojankCITsContext(ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (!CONTAINER.active() || this.citresewn$mojankCITModel == null)
            return;

        if (displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED)
            ((TypeItem.BakedModelManagerMixinAccess) this.itemModelShaper.getModelManager()).citresewn$forceMojankModel(this.citresewn$mojankCITModel.get());

        this.citresewn$mojankCITModel = null;
    }
}
