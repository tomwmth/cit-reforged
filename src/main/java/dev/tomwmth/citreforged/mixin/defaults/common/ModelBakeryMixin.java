package dev.tomwmth.citreforged.mixin.defaults.common;

import com.mojang.datafixers.util.Either;
import dev.tomwmth.citreforged.cit.CITType;
import dev.tomwmth.citreforged.defaults.common.ResewnItemModelIdentifier;
import dev.tomwmth.citreforged.mixin.defaults.types.item.BlockModelAccessor;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Will be rewritten at some point.
 */
@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    @Shadow @Final protected ResourceManager resourceManager;

    @Inject(method = "loadBlockModel", cancellable = true, at = @At("HEAD"))
    public void citresewn$forceLiteralResewnModelIdentifier(ResourceLocation originalId, CallbackInfoReturnable<BlockModel> cir) {
        if (ResewnItemModelIdentifier.marked(originalId)) {
            final ResourceLocation id = ResewnItemModelIdentifier.unpack(originalId);
            try (InputStream is = this.resourceManager.getResource(id).getInputStream()) {
                BlockModel json = BlockModel.fromString(IOUtils.toString(is, StandardCharsets.UTF_8));
                json.name = id.toString();
                json.name = json.name.substring(0, json.name.length() - 5);

                ((BlockModelAccessor) json).getTextureMap().replaceAll((layer, original) -> {
                    Optional<Material> left = original.left();
                    if (left.isPresent()) {
                        String originalPath = left.get().texture().getPath();
                        String[] split = originalPath.split("/");
                        if (originalPath.startsWith("./") || (split.length > 2 && split[1].equals("cit"))) {
                            ResourceLocation resolvedIdentifier = CITType.resolveAsset(id, originalPath, "textures", ".png", this.resourceManager);
                            if (resolvedIdentifier != null)
                                return Either.left(new Material(left.get().atlasLocation(), resolvedIdentifier));
                        }
                    }
                    return original;
                });

                ResourceLocation parentId = ((BlockModelAccessor) json).getParentLocation();
                if (parentId != null) {
                    String[] parentIdPathSplit = parentId.getPath().split("/");
                    if (parentId.getPath().startsWith("./") || (parentIdPathSplit.length > 2 && parentIdPathSplit[1].equals("cit"))) {
                        parentId = CITType.resolveAsset(id, parentId.getPath(), "models", ".json", this.resourceManager);
                        if (parentId != null)
                            ((BlockModelAccessor) json).setParentLocation(ResewnItemModelIdentifier.pack(parentId));
                    }
                }

                json.getOverrides().replaceAll(override -> {
                    String modelIdPath = override.getModel().getPath();
                    String[] modelIdPathSplit = modelIdPath.split("/");
                    if (modelIdPath.startsWith("./") || (modelIdPathSplit.length > 2 && modelIdPathSplit[1].equals("cit"))) {
                        ResourceLocation resolvedOverridePath = CITType.resolveAsset(id, modelIdPath, "models", ".json", this.resourceManager);
                        if (resolvedOverridePath != null)
                            return new ItemOverride(ResewnItemModelIdentifier.pack(resolvedOverridePath), override.getPredicates().collect(Collectors.toList()));
                    }

                    return override;
                });

                cir.setReturnValue(json);
            } catch (Exception ignored) {
            }
        }
    }
}
