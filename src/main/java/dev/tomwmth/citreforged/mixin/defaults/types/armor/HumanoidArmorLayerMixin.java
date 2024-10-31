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
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

        CIT<TypeArmor> cit = CONTAINER.getCIT(new CITContext(equippedStack, entity.level(), entity));
        if (cit != null) {
            this.citresewn$cachedTextures = cit.type.textures;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Redirect(method = "renderArmorPiece", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/ForgeHooksClient;getArmorTexture(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ArmorMaterial$Layer;Z)Lnet/minecraft/resources/ResourceLocation;",
            remap = false
    ))
    private ResourceLocation citresewn$replaceArmorTexture(Entity entity, ItemStack armor, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean inner) {
        if (citresewn$cachedTextures != null) {
            String layerPath = layer.texture(inner).getPath();
            ResourceLocation identifier = citresewn$cachedTextures.get(layerPath.substring("textures/models/armor/".length(), layerPath.length() - ".png".length()));
            if (identifier != null)
                return identifier;
        }
        return ForgeHooksClient.getArmorTexture(entity, armor, slot, layer, inner);
    }
}
