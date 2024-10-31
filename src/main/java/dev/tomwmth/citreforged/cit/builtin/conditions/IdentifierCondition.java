package dev.tomwmth.citreforged.cit.builtin.conditions;

import dev.tomwmth.citreforged.cit.CITCondition;
import dev.tomwmth.citreforged.cit.CITContext;
import dev.tomwmth.citreforged.cit.CITParsingException;
import dev.tomwmth.citreforged.pack.format.PropertyGroup;
import dev.tomwmth.citreforged.pack.format.PropertyKey;
import dev.tomwmth.citreforged.pack.format.PropertyValue;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

/**
 * Common condition parser for identifiers.
 */
public abstract class IdentifierCondition extends CITCondition {
    /**
     * Parsed identifier.
     */
    protected ResourceLocation value;

    /**
	 * Converts the given context to an identifier to compare the parsed value to.
     * @param context context to retrieve the compared value from
	 * @return the identifier value associated with the given context
     */
    protected ResourceLocation getValue(CITContext context) {
        throw new AssertionError("Not implemented by this condition");
    }

    @Override
    public void load(PropertyKey key, PropertyValue value, PropertyGroup properties) throws CITParsingException {
        try {
            this.value = ResourceLocation.tryParse(value.value());
        } catch (ResourceLocationException ex) {
            throw new CITParsingException(ex.getMessage(), properties, value.position());
        }
    }

    @Override
    public boolean test(CITContext context) {
        return this.value.equals(getValue(context));
    }
}
