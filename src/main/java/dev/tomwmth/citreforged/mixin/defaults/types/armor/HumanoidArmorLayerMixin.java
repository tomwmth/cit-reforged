package dev.tomwmth.citreforged.mixin.defaults.types.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tomwmth.citreforged.cit.CIT;
import dev.tomwmth.citreforged.cit.CITContext;
import dev.tomwmth.citreforged.defaults.cit.types.TypeArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static dev.tomwmth.citreforged.defaults.cit.types.TypeArmor.CONTAINER;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Unique private Map<String, ResourceLocation> citresewn$cachedTextures = null;

    @Inject(method = "renderArmorPiece", at = @At("HEAD"))
    public void citresewn$renderArmor(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        this.citresewn$cachedTextures = null;
        if (!CONTAINER.active())
            return;

        ItemStack equippedStack = CONTAINER.getVisualItemInSlot(entity, armorSlot);

        CIT<TypeArmor> cit = CONTAINER.getCIT(new CITContext(equippedStack, entity.getLevel(), entity));
        if (cit != null) {
            this.citresewn$cachedTextures = cit.type.textures;
        }
    }

    // Forge introduces their own method for this that is called instead of the vanilla method
    @Inject(method = "getArmorResource", cancellable = true, at = @At("HEAD"), remap = false)
    private void citresewn$replaceArmorTexture(Entity entity, ItemStack stack, EquipmentSlot slot, String type, CallbackInfoReturnable<ResourceLocation> cir) {
        if (this.citresewn$cachedTextures == null)
            return;

        if (stack.getItem() instanceof ArmorItem armorItem) {
            ResourceLocation identifier = this.citresewn$cachedTextures.get(
                    armorItem.getMaterial().getName() + "_layer_" +
                            (slot == EquipmentSlot.LEGS ? "2" : "1") +
                            (type == null ? "" : "_" + type)
            );
            if (identifier != null) {
                cir.setReturnValue(identifier);
            }
        }
    }
}
