package dev.tomwmth.citreforged.cit;

import dev.tomwmth.citreforged.CITRegistrarImpl;
import dev.tomwmth.citreforged.api.CITDisposable;
import dev.tomwmth.citreforged.api.CITTypeContainer;
import dev.tomwmth.citreforged.cit.builtin.conditions.core.FallbackCondition;
import dev.tomwmth.citreforged.cit.builtin.conditions.core.WeightCondition;
import dev.tomwmth.citreforged.config.CITResewnConfig;
import dev.tomwmth.citreforged.pack.GlobalProperties;
import dev.tomwmth.citreforged.pack.PackParser;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

/**
 * Holds and manages the currently loaded CITs.
 * @see #getActive()
 */
public class ActiveCITs {
    private ActiveCITs() {
    }

	/**
	 * @see #load(ResourceManager, ProfilerFiller)
	 * @see #getActive()
	 * @see #isActive()
	 */
    private static ActiveCITs active = null;

	/**
	 * @see #isActive()
	 * @return the current active CITs manager or null if none are loaded
	 */
    public static ActiveCITs getActive() {
        return active;
    }
	
	/**
	 * @see #getActive()
	 * @return whether there are active; loaded CITs
	 */
    public static boolean isActive() {
        return active != null;
    }

	/**
	 * Currently effective global properties merged from all loaded packs.
	 */
    public final GlobalProperties globalProperties = new GlobalProperties();

	/**
	 * All loaded CITs ordered by their type's class and their weight.
	 */
    public final Map<Class<? extends CITType>, List<CIT<?>>> cits = new IdentityHashMap<>();

	/**
	 * Attempts to load/activate CITs from packs in the given resource manager, disposing of any previously loaded CITs if present.
	 * @see PackParser#loadGlobalProperties(ResourceManager, GlobalProperties)
	 * @see GlobalProperties#callHandlers()
	 * @see PackParser#parseCITs(ResourceManager)
	 * @param resourceManager manager containing resourcepacks with possible CITs
	 * @param profiler loading profiler that was pushed once into "citresewn:reloading_cits" and would pop after
	 */
    public static void load(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("citresewn:disposing");
        for (CITDisposable disposable : CITRegistrarImpl.getDisposables())
            disposable.dispose();

        for (CITTypeContainer<? extends CITType> typeContainer : CITRegistry.TYPES.values())
            typeContainer.unload();

        if (active != null) {
            active.globalProperties.properties.replaceAll((key, value) -> Set.of());
            active.globalProperties.callHandlers();

            active = null;
        }

        if (!CITResewnConfig.INSTANCE.enabled) {
            profiler.pop();
            return;
        }

        ActiveCITs active = new ActiveCITs();

        profiler.popPush("citresewn:load_global_properties");
        PackParser.loadGlobalProperties(resourceManager, active.globalProperties).callHandlers();

        profiler.popPush("citresewn:load_cits");
        List<CIT<?>> cits = PackParser.parseCITs(resourceManager);

        FallbackCondition.apply(cits);

        for (CIT<?> cit : cits)
            active.cits.computeIfAbsent(cit.type.getClass(), type -> new ArrayList<>()).add(cit);

        for (Map.Entry<Class<? extends CITType>, List<CIT<?>>> entry : active.cits.entrySet()) {
            WeightCondition.apply(entry.getValue());

            for (CITTypeContainer<? extends CITType> typeContainer : CITRegistry.TYPES.values())
                if (typeContainer.type == entry.getKey()) {
                    typeContainer.loadUntyped(entry.getValue());
                    break;
                }
        }

        profiler.pop();

        if (!cits.isEmpty())
            ActiveCITs.active = active;
    }
}
