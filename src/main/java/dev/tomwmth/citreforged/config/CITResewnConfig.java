package dev.tomwmth.citreforged.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import dev.tomwmth.citreforged.CITReforged;
import dev.tomwmth.citreforged.Reference;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Contains runtime representation of CIT Resewn's config, encoded using GSON.
 */
public class CITResewnConfig {
    /**
     * Whether CIT Resewn should work or not.<br>
     * Requires a restart.
     */
    public boolean enabled = true;
    /**
     * Mutes pack loading errors from logs.
     */
    public boolean mute_errors = false;
    /**
     * Mutes pack loading warnings from logs.
     */
    public boolean mute_warns = false;
    /**
     * Invalidating interval for CITs' caches in milliseconds. Set to 0 to disable caching.
     */
    public int cache_ms = 50;
    /**
     * Should broken paths be allowed in resourcepacks. Requires a restart.
     * @see BrokenPaths
     */
    public boolean broken_paths = false;

    /**
     * CIT Resewn's config storage file.
     */
    private static final File FILE = FMLPaths.CONFIGDIR.get().resolve(Reference.MOD_ID + ".json").toFile();

    /**
     * Active instance of the current config.
     */
    public static final CITResewnConfig INSTANCE = read();

    /**
     * Reads the stored config.
     * @see #FILE
     * @return the read config
     */
    public static CITResewnConfig read() {
        if (!FILE.exists())
            return new CITResewnConfig().write();

        Reader reader = null;
        try {
            return new Gson().fromJson(reader = new FileReader(FILE), CITResewnConfig.class);
        } catch (Exception ex) {
            CITReforged.LOGGER.error("Failed to read config", ex);
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Saves this config to file.
     * @see #FILE
     * @return this
     */
    public CITResewnConfig write() {
        Gson gson = new Gson();
        JsonWriter writer = null;
        try {
            writer = gson.newJsonWriter(new FileWriter(FILE));
            writer.setIndent("    ");

            gson.toJson(gson.toJsonTree(this, CITResewnConfig.class), writer);
        } catch (Exception ex) {
            CITReforged.LOGGER.error("Failed to save config", ex);
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return this;
    }
}
