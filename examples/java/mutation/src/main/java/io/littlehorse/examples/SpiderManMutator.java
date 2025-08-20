package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpiderManMutator {

    private static final Logger log = LoggerFactory.getLogger(SpiderManMutator.class);

    @LHTaskMethod("spider-bite")
    public String spiderBite(String name) {
        log.debug("Executing spider-bite");
        if (List.of("Miles", "Peter").contains(name)) {
            log.debug("{} got bitten", name);
            return "Spider-man";
        }
        return "The spider bite has no effect on " + name;
    }
}
