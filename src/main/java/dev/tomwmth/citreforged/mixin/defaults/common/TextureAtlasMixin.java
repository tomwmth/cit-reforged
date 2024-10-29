package dev.tomwmth.citreforged.mixin.defaults.common;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author Thomas Wearmouth
 * Created on 29/10/2024
 */
@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {
    @Shadow @Final private static String FILE_EXTENSION;

    @Unique private static final String TEXTURE_DIR = "textures/";

    @ModifyVariable(method = "getSprite", argsOnly = true, at = @At("HEAD"))
    private ResourceLocation citresewn$unwrapTexturePaths(ResourceLocation id) {
        if (id.getPath().endsWith(FILE_EXTENSION)) {
            id = new ResourceLocation(id.getNamespace(), id.getPath().substring(0, id.getPath().length() - FILE_EXTENSION.length()));

            if (id.getPath().startsWith(TEXTURE_DIR)) {
                id = new ResourceLocation(id.getNamespace(), id.getPath().substring(TEXTURE_DIR.length()));
            }
        }
        return id;
    }
}
