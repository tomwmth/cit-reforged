package dev.tomwmth.citreforged.cit.builtin.conditions;

import dev.tomwmth.citreforged.cit.CITCondition;
import dev.tomwmth.citreforged.cit.CITContext;
import dev.tomwmth.citreforged.cit.CITParsingException;
import dev.tomwmth.citreforged.pack.format.PropertyGroup;
import dev.tomwmth.citreforged.pack.format.PropertyValue;

/**
 * Common condition type with no parsing for constant true/false testing output.
 */
public abstract class ConstantCondition extends CITCondition {
    /**
     * Constant condition that always tests positive.
     */
    public static final ConstantCondition TRUE = new ConstantCondition(true) {
        @Override
        public boolean test(CITContext context) {
            return true;
        }
    };

    /**
     * Constant condition that always tests negative.
     */
    public static final ConstantCondition FALSE = new ConstantCondition(false) {
        @Override
        public boolean test(CITContext context) {
            return false;
        }
    };

    /**
     * @return constant condition for the given boolean value
     */
    public static ConstantCondition of(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * What testing contexts will always result in.
     */
    public final boolean value;

    private ConstantCondition(boolean value) {
        this.value = value;
    }

    @Override
    public void load(PropertyValue value, PropertyGroup properties) throws CITParsingException { }
}
