package org.abelsromero.parallels.configuration;

import java.util.Optional;

public class Configuration {

    public static Optional<String> readString(String key) {
        String value = System.getenv(key);
        if (value == null)
            value = System.getProperty(key);
        return Optional.ofNullable(value);
    }

    // TODO handle errors ?
    public static Optional<Integer> readInteger(String key) {
        return readString(key)
            .map(Integer::parseInt);
    }

    // Only supports 'true', no 'yes'
    public static Optional<Boolean> readBoolean(String key) {
        return readString(key)
            .map(Boolean::parseBoolean);
    }
}
