package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;

public abstract class ProcessingContext implements ExecutionContext {

    private final LHServerConfig serverConfig;

    public ProcessingContext(LHServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public final CompletableFuture<Boolean> outputTopicsExist() {
        TenantIdModel tenantId = authorization().tenantId();
        if (tenantId == null) {
            throw new IllegalStateException(
                    "Execution context without a valid Tenant ID. This indicates a bug in the server");
        }
        Map<String, KafkaFuture<TopicDescription>> stringKafkaFutureMap = serverConfig.outputTopicExistsFor(tenantId);
        List<CompletableFuture<Boolean>> topicsExist = new ArrayList<>();
        topicsExist.add(CompletableFuture.completedFuture(true)); // at least one completed future with True
        for (KafkaFuture<TopicDescription> value : stringKafkaFutureMap.values()) {
            CompletableFuture<Boolean> futureTopicExistsAnswer = value.toCompletionStage()
                    .thenApply(Objects::nonNull)
                    // Ensure the future responses with a True value or exception
                    .thenCompose(exists -> exists
                            ? CompletableFuture.completedFuture(true)
                            : CompletableFuture.failedFuture(
                                    new IllegalStateException("Output topic does not exist for tenant " + tenantId)))
                    .toCompletableFuture();
            topicsExist.add(futureTopicExistsAnswer);
        }
        return CompletableFuture.allOf(topicsExist.toArray(new CompletableFuture[0]))
                .thenApply(v -> true)
                .exceptionally(ex -> false);
    }
}
