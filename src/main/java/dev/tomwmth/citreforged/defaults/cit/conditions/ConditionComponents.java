package dev.tomwmth.citreforged.defaults.cit.conditions;

import dev.tomwmth.citreforged.CITReforged;
import dev.tomwmth.citreforged.api.CITConditionContainer;
import dev.tomwmth.citreforged.cit.CITCondition;
import dev.tomwmth.citreforged.cit.CITContext;
import dev.tomwmth.citreforged.cit.CITParsingException;
import dev.tomwmth.citreforged.pack.format.PropertyGroup;
import dev.tomwmth.citreforged.pack.format.PropertyKey;
import dev.tomwmth.citreforged.pack.format.PropertyValue;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("deprecation")
public class ConditionComponents extends CITCondition {
    public static final CITConditionContainer<ConditionComponents> CONTAINER = new CITConditionContainer<>(ConditionComponents.class, ConditionComponents::new,
            "components", "component", "nbt");

    private DataComponentType<?> componentType;
    private String componentMetadata;
    private String matchValue;

    private ConditionNBT fallbackNBTCheck;

    @Override
    public void load(PropertyKey key, PropertyValue value, PropertyGroup properties) throws CITParsingException {
        String metadata = value.keyMetadata();
        if (key.path().equals("nbt")) {
            if (metadata.startsWith("display.Name")) {
                metadata = "minecraft:custom_name" + value.keyMetadata().substring("display.Name".length());
                CITReforged.logWarning(properties.messageWithDescriptorOf("Using legacy nbt.display.Name", value.position()));
            } else if (metadata.startsWith("display.Lore")) {
                metadata = "minecraft:lore" + value.keyMetadata().substring("display.Lore".length());
                CITReforged.logWarning(properties.messageWithDescriptorOf("Using legacy nbt.display.Lore", value.position()));
            } else {
                throw new CITParsingException("NBT condition is not supported in 1.20.5+", properties, value.position());
            }
        }

        metadata = metadata.replace("~", "minecraft:");

        String componentId = metadata.split("\\.")[0];

        if ((this.componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.tryParse(componentId))) == null)
            throw new CITParsingException("Unknown component type \"" + componentId + "\"", properties, value.position());

        metadata = metadata.substring(componentId.length());
        if (metadata.startsWith("."))
            metadata = metadata.substring(1);
        this.componentMetadata = metadata;

        this.matchValue = value.value();

        this.fallbackNBTCheck = new ConditionNBT();
        String[] metadataNbtPath = metadata.split("\\.");
        if (metadataNbtPath.length == 1 && metadataNbtPath[0].isEmpty())
            metadataNbtPath = new String[0];
        this.fallbackNBTCheck.loadNbtCondition(value, properties, metadataNbtPath, this.matchValue);
    }

    @Override
    public boolean test(CITContext context) {
        Object stackComponent = context.stack.getComponents().get(this.componentType);
        if (stackComponent != null) {
            if (stackComponent instanceof Component text) {
                if (this.fallbackNBTCheck.testString(null, text, context))
                    return true;
            } /*else if (stackComponent instanceof LoreComponent lore) {
                //todo avoid nbt based check if possible
            }*/

            Tag fallbackComponentNBT = ((DataComponentType<Object>) this.componentType).codec().encodeStart(context.world.registryAccess().createSerializationContext(NbtOps.INSTANCE), stackComponent).getOrThrow();
            return this.fallbackNBTCheck.testPath(fallbackComponentNBT, 0, context);
        }
        return false;
    }
}
