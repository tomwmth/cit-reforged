package dev.tomwmth.citreforged.cit;

import dev.tomwmth.citreforged.CITReforged;
import dev.tomwmth.citreforged.api.CITConditionContainer;
import dev.tomwmth.citreforged.config.CITResewnConfig;
import dev.tomwmth.citreforged.pack.format.PropertyGroup;
import dev.tomwmth.citreforged.pack.format.PropertyKey;
import dev.tomwmth.citreforged.pack.format.PropertyValue;

import java.util.Collections;
import java.util.Set;

/**
 * Instanced parent for CIT Types that are applied to items when conditions pass.<br>
 * Common condition types are available under {@link dev.tomwmth.citreforged.cit.builtin.conditions}.
 * @see CITConditionContainer
 * @see CIT
 */
public abstract class CITCondition {
    /**
     * Parses the given property value into the condition.
     *
     * @param key        key for this condition
     * @param value      value to read
     * @param properties the group containing value
     * @throws CITParsingException if errored while parsing the condition
     */
    public abstract void load(PropertyKey key, PropertyValue value, PropertyGroup properties) throws CITParsingException;

    /**
     * @return a set of classes of conditions that have integration with this condition
     */
    public Set<Class<? extends CITCondition>> siblingConditions() {
        return Collections.emptySet();
    }

    /**
     * Modifies the given sibling if present in the CIT and declared in {@link #siblingConditions()}.
     * @param sibling sibling to modify or be modified by
     * @return the sibling or null to remove it from the CIT.
     */
    public <T extends CITCondition> T modifySibling(T sibling) {
        return sibling;
    }

    /**
     * Tests the given context against this condition.
     * @param context context to check
     * @return true if the given context passes
     */
    public abstract boolean test(CITContext context);

    /**
     * Logs a warning with the given value's descriptor if enabled in config.
     *
     * @see CITReforged#logWarning(String, Object...)
     * @see CITResewnConfig#mute_warns
     * @param message warning message
     * @param value value associated with the warning
     * @param properties property group associated with the warning
     */
    protected void warn(String message, PropertyValue value, PropertyGroup properties) {
        CITReforged.logWarning("Warning: {}", properties.messageWithDescriptorOf(message, value.position()));
    }
}
