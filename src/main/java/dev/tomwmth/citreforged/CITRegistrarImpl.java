package dev.tomwmth.citreforged;

import dev.tomwmth.citreforged.api.*;
import dev.tomwmth.citreforged.cit.builtin.conditions.core.FallbackCondition;
import dev.tomwmth.citreforged.cit.builtin.conditions.core.WeightCondition;
import dev.tomwmth.citreforged.defaults.cit.conditions.*;
import dev.tomwmth.citreforged.defaults.cit.types.TypeArmor;
import dev.tomwmth.citreforged.defaults.cit.types.TypeElytra;
import dev.tomwmth.citreforged.defaults.cit.types.TypeEnchantment;
import dev.tomwmth.citreforged.defaults.cit.types.TypeItem;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Wearmouth
 * Created on 07/09/2024
 */
public final class CITRegistrarImpl implements CITRegistrar {
    private static final String INBUILT_NAMESPACE = "citresewn";

    private static final List<CITDisposable> DISPOSABLES = new LinkedList<>();
    private static final Map<String, CITGlobalProperties> GLOBAL_PROPERTIES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, List<CITTypeContainer<?>>> TYPES = new LinkedHashMap<>();
    private static final Map<String, List<CITConditionContainer<?>>> CONDITIONS = new LinkedHashMap<>();

    @Override
    public void register(CITDisposable disposable) {
        DISPOSABLES.add(disposable);
    }

    @Override
    public void register(String namespace, CITGlobalProperties globalProperties) {
        GLOBAL_PROPERTIES.put(namespace, globalProperties);
    }

    @Override
    public void register(String namespace, CITTypeContainer<?> type) {
        TYPES.compute(new ResourceLocation(namespace, type.id), (key, val) -> {
            List<CITTypeContainer<?>> value = val == null ? new LinkedList<>() : val;
            value.add(type);
            return value;
        });
    }

    @Override
    public void register(String namespace, CITConditionContainer<?> condition) {
        CONDITIONS.compute(namespace, (key, val) -> {
            List<CITConditionContainer<?>> value = val == null ? new LinkedList<>() : val;
            value.add(condition);
            return value;
        });
    }

    public static List<CITDisposable> getDisposables() {
        return DISPOSABLES;
    }

    public static Map<String, CITGlobalProperties> getGlobalProperties() {
        return GLOBAL_PROPERTIES;
    }

    public static Map<ResourceLocation, List<CITTypeContainer<?>>> getTypes() {
        return TYPES;
    }

    public static Map<String, List<CITConditionContainer<?>>> getConditions() {
        return CONDITIONS;
    }

    static {
        CITRegistrarImpl impl = new CITRegistrarImpl();

        // Base
        impl.register(INBUILT_NAMESPACE, FallbackCondition.PROPS_CONTAINER);
        impl.register(INBUILT_NAMESPACE, FallbackCondition.COND_CONTAINER);
        impl.register(INBUILT_NAMESPACE, WeightCondition.CONTAINER);

        // Defaults
        impl.register(INBUILT_NAMESPACE, ConditionComponents.CONTAINER);
        impl.register(INBUILT_NAMESPACE, ConditionDamage.CONTAINER);
        impl.register(INBUILT_NAMESPACE, ConditionDamageMask.CONTAINER);
        impl.register(INBUILT_NAMESPACE, ConditionEnchantments.CONTAINER);
        impl.register(INBUILT_NAMESPACE, ConditionEnchantmentLevels.CONTAINER);
        impl.register(INBUILT_NAMESPACE, ConditionHand.CONTAINER);
        impl.register(INBUILT_NAMESPACE, ConditionItems.CONTAINER);
        impl.register(INBUILT_NAMESPACE, ConditionStackSize.CONTAINER);

        impl.register(INBUILT_NAMESPACE, TypeArmor.CONTAINER);
        impl.register(INBUILT_NAMESPACE, TypeElytra.CONTAINER);
        impl.register(INBUILT_NAMESPACE, (CITTypeContainer<?>) TypeEnchantment.CONTAINER);
        impl.register(INBUILT_NAMESPACE, (CITGlobalProperties) TypeEnchantment.CONTAINER);
        impl.register(INBUILT_NAMESPACE, TypeItem.CONTAINER);
    }
}
