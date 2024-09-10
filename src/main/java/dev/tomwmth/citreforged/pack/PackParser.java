package dev.tomwmth.citreforged.pack;

import dev.tomwmth.citreforged.CITReforged;
import dev.tomwmth.citreforged.cit.*;
import dev.tomwmth.citreforged.cit.builtin.conditions.core.FallbackCondition;
import dev.tomwmth.citreforged.cit.builtin.conditions.core.WeightCondition;
import dev.tomwmth.citreforged.pack.format.PropertyGroup;
import dev.tomwmth.citreforged.pack.format.PropertyKey;
import dev.tomwmth.citreforged.pack.format.PropertyValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * Utility parsing methods for packs.
 */
public final class PackParser {
    private PackParser() {
    }

    /**
     * Possible CIT roots in resourcepacks ordered in increasing order of priority.
     */
    public static final List<String> ROOTS = List.of("mcpatcher", "optifine", "citresewn");

    /**
     * Loads a merged global property group from loaded packs making sure to respect order.
     *
     * @see GlobalProperties#callHandlers()
     * @param resourceManager the manager that contains the packs
     * @param globalProperties global property group to parse into
     * @return globalProperties
     */
    public static GlobalProperties loadGlobalProperties(ResourceManager resourceManager, GlobalProperties globalProperties) {
        for (PackResources pack : resourceManager.listPacks().toList()) {
            for (String namespace : pack.getNamespaces(PackType.CLIENT_RESOURCES)) {
                for (String root : ROOTS) {
                    ResourceLocation identifier = new ResourceLocation(namespace, root + "/cit.properties");
                    try {
                        IoSupplier<InputStream> propertiesSupplier = pack.getResource(PackType.CLIENT_RESOURCES, identifier);
                        if (propertiesSupplier != null) {
                            globalProperties.load(pack.packId(), identifier, propertiesSupplier.get());
                        }
                    } catch (FileNotFoundException ignored) {
                    } catch (Exception ex) {
                        CITReforged.logError("Errored while loading global properties: {} from {}", identifier, pack.packId());
                    }
                }
            }
        }
        return globalProperties;
    }

    /**
     * Attempts parsing all CITs out of all loaded packs.
     * @param resourceManager the manager that contains the packs
     * @return unordered list of successfully parsed CITs
     */
    public static List<CIT<?>> parseCITs(ResourceManager resourceManager) {
        List<CIT<?>> cits = new ArrayList<>();

        for (String root : ROOTS) {
            for (var entry : resourceManager.listResources(root + "/cit", s -> s.getPath().endsWith(".properties")).entrySet()) {
                String packName = null;
                ResourceLocation key = entry.getKey();
                Resource val = entry.getValue();
                try {
                    cits.add(parseCIT(PropertyGroup.tryParseGroup(packName = val.sourcePackId(), key, val.open()), resourceManager));
                } catch (CITParsingException ex) {
                    CITReforged.logError("Errored while parsing CIT", ex);
                } catch (Exception ex) {
                    CITReforged.logError("Errored while loading CIT: {}{}", key, (packName == null ? "" : " from " + packName), ex);
                }
            }
        }

        return cits;
    }

    /**
     * Attempts parsing a CIT from a property group.
     * @param properties property group representation of the CIT
     * @param resourceManager the manager that contains the property group, used to resolve relative assets
     * @return the successfully parsed CIT
     * @throws CITParsingException if the CIT failed parsing for any reason
     */
    public static CIT<?> parseCIT(PropertyGroup properties, ResourceManager resourceManager) throws CITParsingException {
        CITType citType = CITRegistry.parseType(properties);

        List<CITCondition> conditions = new ArrayList<>();

        Set<PropertyKey> ignoredProperties = citType.typeProperties();

        for (Map.Entry<PropertyKey, Set<PropertyValue>> entry : properties.properties.entrySet()) {
            if (entry.getKey().path().equals("type") && entry.getKey().namespace().equals("citresewn"))
                continue;
            if (ignoredProperties.contains(entry.getKey()))
                continue;

            for (PropertyValue value : entry.getValue())
                conditions.add(CITRegistry.parseCondition(entry.getKey(), value, properties));
        }

        for (CITCondition condition : new ArrayList<>(conditions))
            if (condition != null)
                for (Class<? extends CITCondition> siblingConditionType : condition.siblingConditions())
                    conditions.replaceAll(
                            siblingCondition -> siblingCondition != null && siblingConditionType == siblingCondition.getClass() ?
                                    condition.modifySibling(siblingCondition) :
                                    siblingCondition);

        WeightCondition weight = new WeightCondition();
        FallbackCondition fallback = new FallbackCondition();

        conditions.removeIf(condition -> {
            if (condition instanceof WeightCondition weightCondition) {
                weight.setWeight(weightCondition.getWeight());
                return true;
            } else if (condition instanceof FallbackCondition fallbackCondition) {
                fallback.setFallback(fallbackCondition.getFallback());
                return true;
            }

            return condition == null;
        });

        citType.load(conditions, properties, resourceManager);

        return new CIT<>(properties.identifier, properties.packName, citType, conditions.toArray(new CITCondition[0]), weight.getWeight(), fallback.getFallback());
    }
}
