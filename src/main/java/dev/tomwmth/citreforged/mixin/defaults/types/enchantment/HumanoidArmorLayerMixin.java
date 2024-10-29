package dev.tomwmth.citreforged.mixin.defaults.types.enchantment;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tomwmth.citreforged.cit.CITContext;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.tomwmth.citreforged.defaults.cit.types.TypeEnchantment.CONTAINER;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Inject(method = "renderArmorPiece", at = @At("HEAD"))
    private void citresewn$enchantment$setAppliedContextAndStartApplyingArmor(PoseStack matrices, MultiBufferSource vertexConsumers, T livingEntity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        if (CONTAINER.active())
            CONTAINER.setContext(new CITContext(livingEntity.getItemBySlot(armorSlot), livingEntity.level(), livingEntity)).apply();
    }

    @Inject(method = "renderArmorPiece", at = @At("RETURN"))
    private void citresewn$enchantment$stopApplyingArmor(PoseStack matrices, MultiBufferSource vertexConsumers, T livingEntity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        if (CONTAINER.active())
            CONTAINER.setContext(null);
    }
}
