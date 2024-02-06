package io.littlehorse.canary.kafka;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.prometheus.Measurable;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;

@Slf4j
public class KafkaTopicBootstrap extends Bootstrap implements Measurable {

    private final AdminClient adminClient;

    public KafkaTopicBootstrap(final CanaryConfig config) {
        super(config);

        adminClient = KafkaAdminClient.create(config.toKafkaAdminConfig().toMap());
        Shutdown.addShutdownHook("Topics Creator", adminClient);

        try {
            final NewTopic canaryTopic =
                    new NewTopic(config.getTopicName(), config.getTopicPartitions(), config.getTopicReplicas());

            adminClient.createTopics(List.of(canaryTopic)).all().get();
            log.info("Topics {} created", config.getTopicName());
        } catch (Exception e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.warn(e.getMessage());
            } else {
                throw new CanaryException(e);
            }
        }

        log.trace("Initialized");
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final KafkaClientMetrics kafkaClientMetrics = new KafkaClientMetrics(adminClient);
        Shutdown.addShutdownHook("Topics Creator: Prometheus Exporter", kafkaClientMetrics);
        kafkaClientMetrics.bindTo(registry);
    }
}
