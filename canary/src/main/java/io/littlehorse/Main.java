package io.littlehorse;

import io.littlehorse.common.config.ConfigLoader;
import java.io.IOException;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            log.info("Loading configuration from file '{}'", args[0]);
            System.out.println(ConfigLoader.load(Paths.get(args[0])));
        } else {
            log.info("Loading configuration from env variables");
            System.out.println(ConfigLoader.load());
        }
    }
}
