package dev.tomwmth.citreforged.defaults.cit.conditions;

import dev.tomwmth.citreforged.api.CITConditionContainer;
import dev.tomwmth.citreforged.cit.CITCondition;
import dev.tomwmth.citreforged.cit.CITContext;
import dev.tomwmth.citreforged.cit.builtin.conditions.IntegerCondition;

import java.util.Set;

public class ConditionDamageMask extends IntegerCondition {
    public static final CITConditionContainer<ConditionDamageMask> CONTAINER = new CITConditionContainer<>(ConditionDamageMask.class, ConditionDamageMask::new,
            "damage_mask", "damageMask");

    public ConditionDamageMask() {
        super(false, false, false);
    }

    @Override
    protected int getValue(CITContext context) {
        return 0;
    }

    @Override
    public boolean test(CITContext context) {
        return true;
    }

    public int getMask() {
        return this.min;
    }

    @Override
    public Set<Class<? extends CITCondition>> siblingConditions() {
        return Set.of(ConditionDamage.class);
    }
}
