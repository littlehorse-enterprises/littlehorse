package io.littlehorse.common.config;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
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
     * @return A CanaryConfig with all system configuration
     */
    public static CanaryConfig load() {
        SmallRyeConfig config = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .addDefaultSources()
                .build();
        return new CanaryConfig(toMap(config));
    }

    /**
     * Loads properties in this order:
     * 1. MicroProfile Config configuration file META-INF/microprofile-config.properties in the classpath
     * 2. The given external property file
     * 3. Environment variables
     * 4. System properties
     *
     * @param path Properties location
     * @return A CanaryConfig with all system configuration
     * @throws IOException If properties file was not found
     */
    public static CanaryConfig load(Path path) throws IOException {
        SmallRyeConfig config = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .addDefaultSources()
                .withSources(new PropertiesConfigSource(path.toUri().toURL(), 200))
                .build();
        return new CanaryConfig(toMap(config));
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
