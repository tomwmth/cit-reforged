package dev.tomwmth.citreforged.cit.builtin.conditions.core;

import dev.tomwmth.citreforged.api.CITConditionContainer;
import dev.tomwmth.citreforged.api.CITGlobalProperties;
import dev.tomwmth.citreforged.cit.CIT;
import dev.tomwmth.citreforged.cit.builtin.conditions.IdentifierCondition;
import dev.tomwmth.citreforged.pack.format.PropertyValue;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Core property used to set the fallback of the CIT.<br>
 * When a CIT loads successfully, its fallback CIT gets removed prior to activation.<br>
 * If the main CIT fails to load(or if not loaded at all) the fallback loads as usual.
 * @see #apply(List)
 * @see #globalProperty(String, PropertyValue)
 */
public class FallbackCondition extends IdentifierCondition {
    public static final CITGlobalProperties PROPS_CONTAINER = FallbackCondition::globalProperty;
    public static final CITConditionContainer<FallbackCondition> COND_CONTAINER = new CITConditionContainer<>(FallbackCondition.class, FallbackCondition::new,
            "cit_fallback", "citFallback");

    public FallbackCondition() {
        this.value = null;
    }

    public ResourceLocation getFallback() {
        return this.value;
    }

    public void setFallback(ResourceLocation value) {
        this.value = value;
    }

    /**
     * @see #globalProperty(String, PropertyValue)
     */
    private static boolean fallbackCITResewnRoot = false;

    /**
     * When the global property "citresewn:root_fallback" is set to true, all CITs in the "citresewn"
     * root will automatically have the fallback to the same path in the other roots(optifine/mcpatcher).<br>
     * This behavior is overridden if the CIT specifies a {@link FallbackCondition fallback} manually.
     * @see #apply(List)
     */
    public static void globalProperty(String key, PropertyValue value) throws Exception {
        if (key.equals("root_fallback"))
            fallbackCITResewnRoot = value != null && Boolean.parseBoolean(value.value());
    }

    /**
     * Removes fallback {@link CIT CITs} of all successfully loaded CITs in the given list.<br>
     * @see #globalProperty(String, PropertyValue)
     */
    public static void apply(List<CIT<?>> cits) {
        Map<String, Set<ResourceLocation>> removePacks = new HashMap<>();

        for (CIT<?> cit : cits) {
            Set<ResourceLocation> remove = removePacks.computeIfAbsent(cit.packName, s -> new HashSet<>());
            if (cit.fallback == null) {
                if (fallbackCITResewnRoot && cit.propertiesIdentifier.getPath().startsWith("citresewn/")) {
                    String subPath = cit.propertiesIdentifier.getPath().substring(10);
                    remove.add(new ResourceLocation(cit.propertiesIdentifier.getNamespace(), "optifine/" + subPath));
                    remove.add(new ResourceLocation(cit.propertiesIdentifier.getNamespace(), "mcpatcher/" + subPath));
                }
            } else {
                remove.add(cit.fallback);
            }
        }

        cits.removeIf(cit -> {
            Set<ResourceLocation> remove = removePacks.get(cit.packName);
            return remove != null && remove.contains(cit.propertiesIdentifier);
        });
    }
}
