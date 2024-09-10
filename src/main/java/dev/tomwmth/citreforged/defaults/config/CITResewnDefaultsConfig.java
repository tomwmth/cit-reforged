package dev.tomwmth.citreforged.defaults.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import dev.tomwmth.citreforged.CITReforged;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class CITResewnDefaultsConfig {
    public float type_enchantment_scroll_multiplier = 1f;

    private static final File FILE = new File("config/citresewn-defaults.json");

    public static final CITResewnDefaultsConfig INSTANCE = read();

    public static CITResewnDefaultsConfig read() {
        if (!FILE.exists())
            return new CITResewnDefaultsConfig().write();

        Reader reader = null;
        try {
            return new Gson().fromJson(reader = new FileReader(FILE), CITResewnDefaultsConfig.class);
        } catch (Exception ex) {
            CITReforged.LOGGER.error("Failed to read defaults config", ex);
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public CITResewnDefaultsConfig write() {
        Gson gson = new Gson();
        JsonWriter writer = null;
        try {
            FILE.getParentFile().mkdirs();
            writer = gson.newJsonWriter(new FileWriter(FILE));
            writer.setIndent("    ");

            gson.toJson(gson.toJsonTree(this, CITResewnDefaultsConfig.class), writer);
        } catch (Exception ex) {
            CITReforged.LOGGER.error("Failed to save defaults config", ex);;
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return this;
    }
}
