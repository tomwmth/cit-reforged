package dev.tomwmth.citreforged.mixin.defaults.types.enchantment;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.SortedMap;

/**
 * @author Thomas Wearmouth
 * Created on 07/09/2024
 */
@Mixin(RenderBuffers.class)
public interface RenderBuffersAccessor {
    @Accessor
    SortedMap<RenderType, BufferBuilder> getFixedBuffers();
}
