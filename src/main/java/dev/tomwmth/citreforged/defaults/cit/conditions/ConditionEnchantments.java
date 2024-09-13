package dev.tomwmth.citreforged.defaults.cit.conditions;

import dev.tomwmth.citreforged.api.CITConditionContainer;
import dev.tomwmth.citreforged.cit.CITCondition;
import dev.tomwmth.citreforged.cit.CITContext;
import dev.tomwmth.citreforged.cit.CITParsingException;
import dev.tomwmth.citreforged.cit.builtin.conditions.IdentifierCondition;
import dev.tomwmth.citreforged.cit.builtin.conditions.ListCondition;
import dev.tomwmth.citreforged.pack.format.PropertyGroup;
import dev.tomwmth.citreforged.pack.format.PropertyValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class ConditionEnchantments extends ListCondition<ConditionEnchantments.EnchantmentCondition> {
    public static final CITConditionContainer<ConditionEnchantments> CONTAINER = new CITConditionContainer<>(ConditionEnchantments.class, ConditionEnchantments::new,
            "enchantments", "enchantmentIDs");

    public ConditionEnchantments() {
        super(EnchantmentCondition.class, EnchantmentCondition::new);
    }

    public ResourceLocation[] getEnchantments() {
        ResourceLocation[] enchantments = new ResourceLocation[this.conditions.length];

        for (int i = 0; i < this.conditions.length; i++)
            enchantments[i] = this.conditions[i].getValue(null);

        return enchantments;
    }

    @Override
    public Set<Class<? extends CITCondition>> siblingConditions() {
        return Set.of(ConditionEnchantmentLevels.class);
    }

    protected static class EnchantmentCondition extends IdentifierCondition {
        @Override
        public void load(PropertyValue value, PropertyGroup properties) throws CITParsingException {
            super.load(value, properties);

            if (!BuiltInRegistries.ENCHANTMENT.containsKey(this.value))
                warn(this.value + " is not in the enchantment registry", value, properties);
        }

        @Override
        public boolean test(CITContext context) {
            return context.enchantments().containsKey(this.value);
        }

        @Override
        protected ResourceLocation getValue(CITContext context) {
            return this.value;
        }
    }
}
