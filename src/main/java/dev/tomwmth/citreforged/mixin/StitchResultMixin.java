package dev.tomwmth.citreforged.mixin;

import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author Thomas Wearmouth
 * Created on 29/10/2024
 */
@Mixin(AtlasSet.StitchResult.class)
public abstract class StitchResultMixin {
    @Unique private static final String PNG_EXTENSION = ".png";
    @Unique private static final String TEXTURE_DIR = "textures/";

    @ModifyVariable(method = "getSprite", argsOnly = true, at = @At("HEAD"))
    private ResourceLocation citresewn$unwrapTexturePaths(ResourceLocation id) {
        if (id.getPath().endsWith(PNG_EXTENSION)) {
            id = id.withPath(path -> path.substring(0, path.length() - PNG_EXTENSION.length()));

            if (id.getPath().startsWith(TEXTURE_DIR)) {
                id = id.withPath(path -> path.substring(TEXTURE_DIR.length()));
            }
        }
        return id;
    }
}
