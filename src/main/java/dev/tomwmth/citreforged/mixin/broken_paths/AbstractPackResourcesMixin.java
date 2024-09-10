package dev.tomwmth.citreforged.mixin.broken_paths;

import dev.tomwmth.citreforged.config.BrokenPaths;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

/**
 * Adds a resourcepack compatibility error message when broken paths are enabled and are detected in a pack.
 * @see BrokenPaths
 * @see PackCompatibilityMixin
 */
@Mixin(AbstractPackResources.class)
public abstract class AbstractPackResourcesMixin implements PackResources {
    @Shadow @Final public File file;

    @SuppressWarnings({"unchecked"})
    @Inject(method = "getMetadataSection", cancellable = true, at = @At("RETURN"))
    public <T extends PackMetadataSection> void citresewn$brokenpaths$parseMetadata(MetadataSectionSerializer<T> metaReader, CallbackInfoReturnable<T> cir) {
        if (cir.getReturnValue() != null)
            try {
                if (this.getClass().equals(FilePackResources.class)) {
                    try (ZipFile zipFile = new ZipFile(this.file)) {
                        zipFile.stream()
                                .forEach(entry -> {
                                    if (entry.getName().startsWith("assets") && !entry.getName().endsWith(".DS_Store"))
                                        new ResourceLocation("minecraft", entry.getName());
                                });
                    }
                } else if (this.getClass().equals(FolderPackResources.class)) {
                    final Path assets = new File(this.file, "assets").toPath();
                    Files.walk(assets)
                            .forEach(fullPath -> {
                                String path = assets.relativize(fullPath).toString().replace('\\', '/');
                                if (!path.endsWith(".DS_Store"))
                                    new ResourceLocation("minecraft", path);
                            });
                }
            } catch (ResourceLocationException e) {
                cir.setReturnValue((T) new PackMetadataSection(cir.getReturnValue().getDescription(), Integer.MAX_VALUE - 53));
            } catch (Exception ignored) {}
    }
}
