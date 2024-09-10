package dev.tomwmth.citreforged.config;

import dev.tomwmth.citreforged.Reference;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin configuration for CIT Resewn's mixins.
 */
public class CITResewnMixinConfiguration implements IMixinConfigPlugin {
    private static final String MIXINS_ROOT = Reference.MOD_GROUP + "." + Reference.MOD_ID + ".mixin";

    /**
     * Is Broken Paths enabled in config.
     * @see BrokenPaths
     * @see CITResewnConfig#broken_paths
     */
    private boolean broken_paths;

    @Override
    public void onLoad(String mixinPackage) {
        CITResewnConfig launchConfig = CITResewnConfig.read();

        this.broken_paths = launchConfig.broken_paths;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MIXINS_ROOT))
            return false;
        mixinClassName = mixinClassName.substring(MIXINS_ROOT.length() + 1);

        if (mixinClassName.startsWith("broken_paths"))
            return broken_paths;

        return true;
    }


    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // NO-OP
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }
}
