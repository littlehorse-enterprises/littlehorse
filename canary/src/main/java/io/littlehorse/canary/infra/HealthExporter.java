package io.littlehorse.canary.infra;

import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HealthExporter implements WebServiceBinder {
    private final String healthPath;
    private final HealthStatusRegistry healthStatusRegistry;

    public HealthExporter(final String healthPath) {
        this.healthPath = healthPath;
        this.healthStatusRegistry = new HealthStatusRegistry();
    }

    @Override
    public void bindTo(final WebServiceRegistry registry) {
        registry.get(healthPath, ctx -> {
            log.info("Processing health request");
            ctx.status(getStatus());
        });
    }

    private HttpStatus getStatus() {
        return healthStatusRegistry.getStatuses().values().stream().allMatch(HealthStatusSupplier::isHealthy)
                ? HttpStatus.OK
                : HttpStatus.SERVICE_UNAVAILABLE;
    }

    public void addStatus(final HealthStatusBinder status) {
        status.bindTo(healthStatusRegistry);
    }
}
