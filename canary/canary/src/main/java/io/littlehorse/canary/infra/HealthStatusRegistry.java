package io.littlehorse.canary.infra;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class HealthStatusRegistry {

    private final Map<String, HealthStatusSupplier> statuses = new HashMap<>();

    public void addStatus(final String name, final HealthStatusSupplier supplier) {
        // prevents any error
        statuses.put(name, () -> {
            try {
                return supplier.isHealthy();
            } catch (Exception e) {
                log.error("Error getting health status for {}", name, e);
                return false;
            }
        });
    }
}
