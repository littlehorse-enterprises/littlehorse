package io.littlehorse.canary.kafka;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.util.Shutdown;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;

@Slf4j
public class KafkaTopicBootstrap implements Bootstrap {

    public KafkaTopicBootstrap(
            final String metricsTopicName,
            final int topicPartitions,
            final short topicReplicas,
            final Map<String, Object> kafkaAdminConfigMap) {

        final AdminClient adminClient = KafkaAdminClient.create(kafkaAdminConfigMap);
        Shutdown.addShutdownHook(adminClient);

        try {
            final NewTopic canaryTopic = new NewTopic(metricsTopicName, topicPartitions, topicReplicas);

            adminClient.createTopics(List.of(canaryTopic)).all().get();
            log.info("Topics {} created", metricsTopicName);
        } catch (Exception e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.warn(e.getMessage());
            } else {
                throw new CanaryException(e);
            }
        }

        log.trace("Initialized");
    }
}
