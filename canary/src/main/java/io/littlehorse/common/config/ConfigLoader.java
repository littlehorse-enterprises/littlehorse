package io.littlehorse.common.config;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.microprofile.config.Config;

public class ConfigLoader {

    private ConfigLoader() {}

    /**
     * Loads properties in this order:
     * 1. MicroProfile Config configuration file META-INF/microprofile-config.properties in the classpath
     * 2. Environment variables
     * 3. System properties
     *
     * @return A map with all system configuration
     */
    public static Map<String, Object> load() {
        return toMap(new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .addDefaultSources()
                .build());
    }

    /**
     * Loads properties in this order:
     * 1. MicroProfile Config configuration file META-INF/microprofile-config.properties in the classpath
     * 2. The given external property file
     * 3. Environment variables
     * 4. System properties
     *
     * @param path Properties location
     * @return A map with all system configuration
     * @throws IOException If properties file was not found
     */
    public static Map<String, Object> load(Path path) throws IOException {
        SmallRyeConfigBuilder configBuilder =
                new SmallRyeConfigBuilder().addDefaultInterceptors().addDefaultSources();

        if (path != null) {
            configBuilder.withSources(new PropertiesConfigSource(path.toUri().toURL(), 200));
        }

        return toMap(configBuilder.build());
    }

    private static Map<String, Object> toMap(Config config) {
        Map<String, Object> configs = new TreeMap<>();
        for (String key : config.getPropertyNames()) {
            if (!key.startsWith("canary")) {
                continue;
            }
            configs.put(key, config.getOptionalValue(key, String.class).orElse(""));
        }
        return configs;
    }
}
