package dev.tomwmth.citreforged.cit;

import dev.tomwmth.citreforged.CITReforged;
import dev.tomwmth.citreforged.api.CITTypeContainer;
import dev.tomwmth.citreforged.pack.format.PropertyGroup;
import dev.tomwmth.citreforged.pack.format.PropertyKey;
import dev.tomwmth.citreforged.pack.format.PropertyValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.*;

/**
 * Instanced parent for CIT Types that are applied to items when conditions pass.
 * @see CITTypeContainer
 * @see CIT
 */
public abstract class CITType {
    /**
     * Used to determine which property keys are not conditions.
     * @return a set of property keys used by this type
     */
    public abstract Set<PropertyKey> typeProperties();

    /**
     * Loads the given property group into the type.
     * @param conditions conditions that were parsed out of the property group
     * @param properties group of properties to be read into this type
     * @param resourceManager the CIT's containing resource manager
     * @throws CITParsingException if errored while parsing the type
     */
    public abstract void load(List<CITCondition> conditions, PropertyGroup properties, ResourceManager resourceManager) throws CITParsingException;

    protected void warn(String message, PropertyValue value, PropertyGroup properties) {
        CITReforged.logWarning("Warning: {}", properties.messageWithDescriptorOf(message, value == null ? -1 : value.position()));
    }

    /**
     * ///// PORTED FROM BETA \\\\\
     * This shit was ported from the
     * beta and will be rewritten at
     * some point!
     * \\\\\                  /////
     *
     * Takes a defined path and resolves it to an identifier pointing to the resourcepack's path of the specified extension(returns null if no path can be resolved).<br>
     * If definedPath is null, will try to resolve a relative file with the same name as the rootIdentifier with the extension, otherwise: <br>
     * definedPath will be formatted to replace "\\" with "/" the extension will be appended if not there already. <br>
     * It will first try using definedPath as an absolute path, if it cant resolve(or definedPath starts with ./), definedPath will be considered relative. <br>
     * Relative paths support going to parent directories using "..".
     */
    public static ResourceLocation resolveAsset(ResourceLocation rootIdentifier, String path, String defaultedTypeDirectory, String extension, ResourceManager resourceManager) {
        if (path == null) {
            path = rootIdentifier.getPath().substring(0, rootIdentifier.getPath().length() - 11);
            if (!path.endsWith(extension))
                path = path + extension;
            ResourceLocation pathIdentifier = new ResourceLocation(rootIdentifier.getNamespace(), path);
            return resourceManager.hasResource(pathIdentifier) ? pathIdentifier : null;
        }

        ResourceLocation pathIdentifier = new ResourceLocation(path);

        path = pathIdentifier.getPath().replace('\\', '/');
        if (!path.endsWith(extension))
            path = path + extension;

        if (path.startsWith("./"))
            path = path.substring(2);
        else if (!path.contains("..")) {
            pathIdentifier = new ResourceLocation(pathIdentifier.getNamespace(), path);
            if (resourceManager.hasResource(pathIdentifier))
                return pathIdentifier;
            else if (path.startsWith("assets/")) {
                path = path.substring(7);
                int sep = path.indexOf('/');
                pathIdentifier = new ResourceLocation(path.substring(0, sep), path.substring(sep + 1));
                if (resourceManager.hasResource(pathIdentifier))
                    return pathIdentifier;
            }
            pathIdentifier = new ResourceLocation(pathIdentifier.getNamespace(), defaultedTypeDirectory + "/" + path);
            if (resourceManager.hasResource(pathIdentifier))
                return pathIdentifier;
        }

        LinkedList<String> pathParts = new LinkedList<>(Arrays.asList(rootIdentifier.getPath().split("/")));
        pathParts.removeLast();

        if (path.contains("/")) {
            for (String part : path.split("/")) {
                if (part.equals("..")) {
                    if (pathParts.isEmpty())
                        return null;
                    pathParts.removeLast();
                } else
                    pathParts.addLast(part);
            }
        } else
            pathParts.addLast(path);
        path = String.join("/", pathParts);

        pathIdentifier = new ResourceLocation(rootIdentifier.getNamespace(), path);

        return resourceManager.hasResource(pathIdentifier) ? pathIdentifier : null;
    }

    public static ResourceLocation resolveAsset(ResourceLocation rootIdentifier, PropertyValue path, String defaultedTypeDirectory, String extension, ResourceManager resourceManager) {
        return resolveAsset(rootIdentifier, path == null ? null : path.value(), defaultedTypeDirectory, extension, resourceManager);
    }
}
