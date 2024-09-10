package dev.tomwmth.citreforged.config;

import dev.tomwmth.citreforged.mixin.broken_paths.AbstractPackResourcesMixin;
import dev.tomwmth.citreforged.mixin.broken_paths.ReloadableResourceManagerMixin;
import dev.tomwmth.citreforged.mixin.broken_paths.ResourceLocationMixin;
import net.minecraft.resources.ResourceLocation;

/**
 * Broken paths are resourcepack file paths that do not follow {@link ResourceLocation}'s specifications.<br>
 * When enabled in config, CIT Resewn will forcibly allow broken paths to load.<br>
 * If not enabled, broken paths has no effect on the game.
 * @see CITResewnConfig#broken_paths
 * @see ReloadableResourceManagerMixin
 * @see ResourceLocationMixin
 * @see AbstractPackResourcesMixin
 */
public class BrokenPaths {
    /**
     * When enabled, {@link ResourceLocation}s will not check for their path's validity.
     * @see ReloadableResourceManagerMixin
     * @see ResourceLocationMixin
     */
    public static boolean processingBrokenPaths = false;
}
