package dev.tomwmth.citreforged.pack;

import dev.tomwmth.citreforged.CITReforged;
import dev.tomwmth.citreforged.CITRegistrarImpl;
import dev.tomwmth.citreforged.api.CITGlobalProperties;
import dev.tomwmth.citreforged.pack.format.PropertyGroup;
import dev.tomwmth.citreforged.pack.format.PropertyKey;
import dev.tomwmth.citreforged.pack.format.PropertyValue;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Property group representation of the global cit.properties file.
 * @see CITGlobalProperties
 * @see PackParser#loadGlobalProperties(ResourceManager, GlobalProperties)
 */
public class GlobalProperties extends PropertyGroup {
    public GlobalProperties() {
        super("global_properties", new ResourceLocation("citresewn", "global_properties"));
    }

    @Override
    public String getExtension() {
        return ".properties";
    }

    @Override
    public PropertyGroup load(String packName, ResourceLocation identifier, InputStream is) throws IOException, ResourceLocationException {
        PropertyGroup group = PropertyGroup.tryParseGroup(packName, identifier, is);
        if (group != null)
            for (Map.Entry<PropertyKey, Set<PropertyValue>> entry : group.properties.entrySet())
                this.properties.computeIfAbsent(entry.getKey(), key -> new LinkedHashSet<>()).addAll(entry.getValue());

        return this;
    }

    /**
     * Calls all {@link CITGlobalProperties} handler entrypoints for every global property they're associated with.<br>
     * Global properties are matched to their entrypoints by mod id and it's the handler responsibility to filter the properties.
     *
     * @see CITGlobalProperties
     */
    public void callHandlers() {
        CITRegistrarImpl.getGlobalProperties().forEach((namespace, globalProperties) -> {
            for (Map.Entry<PropertyKey, Set<PropertyValue>> entry : this.properties.entrySet()) {
                if (entry.getKey().namespace().equals(namespace)) {
                    PropertyValue lastValue = null;
                    for (PropertyValue value : entry.getValue())
                        lastValue = value;

                    try {
                        globalProperties.globalProperty(entry.getKey().path(), lastValue);
                    } catch (Exception ex) {
                        if (lastValue == null) {
                            CITReforged.LOGGER.error("Errored while disposing global properties");
                        }
                        else {
                            CITReforged.LOGGER.error("Errored while parsing global properties: Line " + lastValue.position() + " of " + lastValue.propertiesIdentifier() + " in " + lastValue.packName());
                        }
                    }
                }
            }
        });
    }
}
