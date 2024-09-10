package dev.tomwmth.citreforged.mixin.defaults.types.item;

import dev.tomwmth.citreforged.CITReforged;
import dev.tomwmth.citreforged.cit.CIT;
import dev.tomwmth.citreforged.defaults.cit.types.TypeItem;
import dev.tomwmth.citreforged.defaults.common.ResewnItemModelIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.BiFunction;

import static dev.tomwmth.citreforged.defaults.cit.types.TypeItem.CONTAINER;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Shadow @Final private Map<ResourceLocation, BakedModel> bakedTopLevelModels;

    @Unique private final ResourceManager citresewn$resourceManager = Minecraft.getInstance().getResourceManager();

    @Redirect(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
            ordinal = 0
    ))
    public void citresewn$addTypeItemModels(ProfilerFiller instance, String s) {
        instance.popPush("citresewn:type_item_models");
        if (CONTAINER.active()) {
            CITReforged.LOGGER.info("Loading item CIT models...");
            for (CIT<TypeItem> cit : CONTAINER.loaded)
                try {
                    cit.type.loadUnbakedAssets(this.citresewn$resourceManager);

                    for (BlockModel unbakedModel : cit.type.unbakedAssets.values()) {
                        ResourceLocation id = ResewnItemModelIdentifier.pack(new ResourceLocation(unbakedModel.name));
                        this.unbakedCache.put(id, unbakedModel);
                        this.topLevelModels.put(id, unbakedModel);
                    }
                } catch (Exception ex) {
                    CITReforged.logError("Errored loading model in {} from {}", cit.propertiesIdentifier, cit.packName, ex);
                }

            TypeItem.GENERATED_SUB_CITS_SEEN.clear();
        }
        instance.popPush(s);
    }

    @Inject(method = "bakeModels", at = @At("RETURN"))
    public void citresewn$linkTypeItemModels(BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteLoader, CallbackInfo ci) {
        if (!CONTAINER.active())
            return;

        CITReforged.LOGGER.info("Linking baked models to item CITs...");
        for (CIT<TypeItem> cit : CONTAINER.loaded) {
            for (Map.Entry<List<ItemOverride.Predicate>, BlockModel> citModelEntry : cit.type.unbakedAssets.entrySet()) {
                if (citModelEntry.getKey() == null) {
                    cit.type.bakedModel = this.bakedTopLevelModels.get(ResewnItemModelIdentifier.pack(new ResourceLocation(citModelEntry.getValue().name)));
                } else {
                    BakedModel bakedModel = this.bakedTopLevelModels.get(ResewnItemModelIdentifier.pack(new ResourceLocation(citModelEntry.getValue().name)));
                    if (bakedModel == null)
                        CITReforged.logWarning("Skipping sub CIT: Failed loading model for \"{}\" in {} from {}", citModelEntry.getValue().name, cit.propertiesIdentifier, cit.packName);
                    else
                        cit.type.bakedSubModels.override(citModelEntry.getKey(), bakedModel);
                }
            }
            cit.type.unbakedAssets = null;
        }
    }

    @ModifyArg(method = "loadBlockModel", at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
            ordinal = 1
    ))
    public Object citresewn$fixDuplicatePrefixSuffix(Object key) {
        ResourceLocation original = (ResourceLocation) key;
        if (CONTAINER.active() && original.getPath().startsWith("models/models/") && original.getPath().endsWith(".json.json") && original.getPath().contains("cit"))
            return new ResourceLocation(original.getNamespace(), original.getPath().substring(7, original.getPath().length() - 5));

        return original;
    }
}
