package com.mbio.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads values from config.properties.
 * A value passed on the command line (-Dkey=value) always wins over the file.
 */
public final class ConfigReader {

    private static final String CONFIG_PATH = "src/test/resources/config.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (FileInputStream input = new FileInputStream(CONFIG_PATH)) {
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load " + CONFIG_PATH, e);
        }
    }

    private ConfigReader() {
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = PROPERTIES.getProperty(key);
        }
        if (value == null) {
            throw new RuntimeException("Key '" + key + "' not found in " + CONFIG_PATH);
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
