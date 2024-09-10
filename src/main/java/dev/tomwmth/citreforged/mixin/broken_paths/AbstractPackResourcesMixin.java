package dev.tomwmth.citreforged.mixin.broken_paths;

import dev.tomwmth.citreforged.config.BrokenPaths;
import net.minecraft.ResourceLocationException;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a resourcepack compatibility error message when broken paths are enabled and are detected in a pack.
 * @see BrokenPaths
 * @see PackCompatibilityMixin
 */
@Mixin(AbstractPackResources.class)
public abstract class AbstractPackResourcesMixin implements PackResources {
    @SuppressWarnings({"unchecked"})
    @Inject(method = "getMetadataSection", cancellable = true, at = @At("RETURN"))
    public <T extends PackMetadataSection> void citresewn$brokenpaths$parseMetadata(MetadataSectionSerializer<T> metaReader, CallbackInfoReturnable<T> cir) {
        if (cir.getReturnValue() != null) {
            try {
                for (String namespace : this.getNamespaces(PackType.CLIENT_RESOURCES)) {
                    this.listResources(PackType.CLIENT_RESOURCES, namespace, "", (id, streamSupplier) -> {});
                }
            }
            catch (ResourceLocationException ex) {
                cir.setReturnValue((T) new PackMetadataSection(cir.getReturnValue().getDescription(), Integer.MAX_VALUE - 53));
            }
            catch (Exception ignored) {}
        }
    }
}
