package dev.tomwmth.citreforged.mixin.defaults.types.enchantment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import dev.tomwmth.citreforged.cit.CITContext;
import dev.tomwmth.citreforged.defaults.cit.types.TypeEnchantment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.tomwmth.citreforged.defaults.cit.types.TypeEnchantment.CONTAINER;

@Mixin(value = ItemRenderer.class, priority = 200)
public abstract class ItemRendererMixin {
    @Inject(method = "getModel", at = @At("HEAD"))
    private void citresewn$enchantment$setAppliedContext(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (CONTAINER.active())
            CONTAINER.setContext(new CITContext(stack, world, entity));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void citresewn$enchantment$startApplyingItem(ItemStack stack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (CONTAINER.active())
            CONTAINER.apply();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void citresewn$enchantment$stopApplyingItem(ItemStack stack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (CONTAINER.active())
            CONTAINER.setContext(null);
    }

    @Inject(method = "getArmorFoilBuffer", cancellable = true, at = @At("RETURN"))
    private static void citresewn$enchantment$getArmorGlintConsumer(MultiBufferSource provider, RenderType layer, boolean solid, boolean glint, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CONTAINER.shouldApply())
            return;
        VertexConsumer vertexConsumer = solid ? TypeEnchantment.GlintRenderLayer.ARMOR_GLINT.tryApply(cir.getReturnValue(), layer, provider) : TypeEnchantment.GlintRenderLayer.ARMOR_ENTITY_GLINT.tryApply(cir.getReturnValue(), layer, provider);
        if (vertexConsumer != null)
            cir.setReturnValue(vertexConsumer);
    }

    @Inject(method = "getCompassFoilBuffer", cancellable = true, at = @At("RETURN"))
    private static void citresewn$enchantment$getCompassGlintConsumer(MultiBufferSource provider, RenderType layer, PoseStack.Pose entry, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CONTAINER.shouldApply())
            return;
        VertexConsumer vertexConsumer = TypeEnchantment.GlintRenderLayer.GLINT.tryApply(null, layer, provider);
        if (vertexConsumer != null)
            cir.setReturnValue(VertexMultiConsumer.create(new SheetedDecalTextureGenerator(vertexConsumer, entry.pose(), entry.normal()), cir.getReturnValue()));
    }

    @Inject(method = "getCompassFoilBufferDirect", cancellable = true, at = @At("RETURN"))
    private static void citresewn$enchantment$getDirectCompassGlintConsumer(MultiBufferSource provider, RenderType layer, PoseStack.Pose entry, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CONTAINER.shouldApply())
            return;
        VertexConsumer vertexConsumer = TypeEnchantment.GlintRenderLayer.DIRECT_GLINT.tryApply(null, layer, provider);
        if (vertexConsumer != null)
            cir.setReturnValue(VertexMultiConsumer.create(new SheetedDecalTextureGenerator(vertexConsumer, entry.pose(), entry.normal()), cir.getReturnValue()));
    }

    @Inject(method = "getFoilBuffer", cancellable = true, at = @At("RETURN"))
    private static void citresewn$enchantment$getItemGlintConsumer(MultiBufferSource provider, RenderType layer, boolean solid, boolean glint, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CONTAINER.shouldApply())
            return;
        VertexConsumer vertexConsumer = Minecraft.useShaderTransparency() && layer == Sheets.translucentItemSheet() ? TypeEnchantment.GlintRenderLayer.GLINT_TRANSLUCENT.tryApply(cir.getReturnValue(), layer, provider) : (solid ? TypeEnchantment.GlintRenderLayer.GLINT.tryApply(cir.getReturnValue(), layer, provider) : TypeEnchantment.GlintRenderLayer.ENTITY_GLINT.tryApply(cir.getReturnValue(), layer, provider));
        if (vertexConsumer != null)
            cir.setReturnValue(vertexConsumer);
    }

    @Inject(method = "getFoilBufferDirect", cancellable = true, at = @At("RETURN"))
    private static void citresewn$enchantment$getDirectItemGlintConsumer(MultiBufferSource provider, RenderType layer, boolean solid, boolean glint, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CONTAINER.shouldApply())
            return;
        VertexConsumer vertexConsumer = solid ? TypeEnchantment.GlintRenderLayer.DIRECT_GLINT.tryApply(cir.getReturnValue(), layer, provider) : TypeEnchantment.GlintRenderLayer.DIRECT_ENTITY_GLINT.tryApply(cir.getReturnValue(), layer, provider);
        if (vertexConsumer != null)
            cir.setReturnValue(vertexConsumer);
    }
}