package io.littlehorse;

import io.littlehorse.common.config.CanaryConfig;
import io.littlehorse.common.config.ConfigLoader;
import java.io.IOException;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) throws IOException {
        CanaryConfig config = args.length > 0 ? ConfigLoader.load(Paths.get(args[0])) : ConfigLoader.load();
        log.info("Configurations: {}", config);
    }
}
