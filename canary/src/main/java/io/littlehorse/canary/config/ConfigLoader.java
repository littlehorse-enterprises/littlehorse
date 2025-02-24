package io.littlehorse.canary.config;

import com.google.common.collect.Streams;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.common.utils.ConfigSourceUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        final SmallRyeConfig config = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .addDefaultSources()
                .withDefaultValues(defaultValues())
                .build();
        return new CanaryConfig(toMap(config));
    }

    /**
     * Loads properties in this order:
     * 1. MicroProfile Config configuration file META-INF/microprofile-config.properties in the classpath
     * 2. The given external properties file
     * 3. Environment variables
     * 4. System properties
     *
     * @param path Properties location
     * @return A CanaryConfig with all system configuration
     * @throws IOException If properties file was not found
     */
    public static CanaryConfig load(final Path path) throws IOException {
        final SmallRyeConfig config = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .addDefaultSources()
                .withSources(new PropertiesConfigSource(path.toUri().toURL(), 200))
                .withDefaultValues(defaultValues())
                .build();
        return new CanaryConfig(toMap(config));
    }

    /**
     * Loads properties in this order:
     * 1. MicroProfile Config configuration file META-INF/microprofile-config.properties in the classpath
     * 2. The given Properties object
     * 3. Environment variables
     * 4. System properties
     *
     * @param properties Properties object
     * @return A CanaryConfig with all system configuration
     */
    public static CanaryConfig load(final Properties properties) {
        final SmallRyeConfig config = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .addDefaultSources()
                .withSources(new PropertiesConfigSource(
                        ConfigSourceUtil.propertiesToMap(properties), "PropertiesConfigSource[source=Properties]", 200))
                .withDefaultValues(defaultValues())
                .build();
        return new CanaryConfig(toMap(config));
    }

    private static Map<String, Object> toMap(final Config config) {
        return Streams.stream(config.getPropertyNames())
                .collect(Collectors.toUnmodifiableMap(
                        Function.identity(),
                        key -> config.getOptionalValue(key, String.class).orElse("")));
    }

    private static Map<String, String> defaultValues() {
        return Map.of();
    }
}
