package io.littlehorse.canary.kafka;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.CanaryException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;

@Slf4j
public class KafkaTopicBootstrap implements Bootstrap {

    private final AdminClient adminClient;

    public KafkaTopicBootstrap(
            String topicName, int topicPartitions, short topicReplicas, Map<String, Object> kafkaAdminConfigMap) {
        adminClient = KafkaAdminClient.create(kafkaAdminConfigMap);

        try {
            NewTopic canaryTopic = new NewTopic(topicName, topicPartitions, topicReplicas);

            adminClient.createTopics(List.of(canaryTopic)).all().get();
            log.info("Topics {} created", topicName);
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
    public void shutdown() {
        adminClient.close();
        log.trace("Shutdown");
    }
}
