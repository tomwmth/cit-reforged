package dev.tomwmth.citreforged;

import dev.tomwmth.citreforged.Reference;
import dev.tomwmth.citreforged.cit.CITRegistry;
import dev.tomwmth.citreforged.config.CITResewnConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Thomas Wearmouth
 * Created on 07/09/2024
 */
@Mod(value = Reference.MOD_ID)
public final class CITReforged {
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    public CITReforged(FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener((FMLConstructModEvent ev) -> CITRegistry.registerAll());
    }

    public static void logWarning(String message, Object... params) {
        if (CITResewnConfig.INSTANCE.mute_warns)
            return;
        LOGGER.warn(message, params);
    }

    public static void logError(String message, Object... params) {
        if (CITResewnConfig.INSTANCE.mute_errors)
            return;
        LOGGER.error(message, params);
    }
}
