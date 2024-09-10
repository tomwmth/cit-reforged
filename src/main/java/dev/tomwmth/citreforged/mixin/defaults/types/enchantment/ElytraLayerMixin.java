package dev.tomwmth.citreforged.mixin.defaults.types.enchantment;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tomwmth.citreforged.cit.CITContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.tomwmth.citreforged.defaults.cit.types.TypeEnchantment.CONTAINER;

@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"))
    private void citresewn$enchantment$setAppliedContextAndStartApplyingElytra(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (CONTAINER.active())
            CONTAINER.setContext(new CITContext(livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.getLevel(), livingEntity)).apply();
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("RETURN"))
    private void citresewn$enchantment$stopApplyingElytra(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (CONTAINER.active())
            CONTAINER.setContext(null);
    }
}
