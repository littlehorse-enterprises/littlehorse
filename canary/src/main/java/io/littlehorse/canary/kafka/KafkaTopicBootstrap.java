package io.littlehorse.canary.kafka;

import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.util.ShutdownHook;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;

@Slf4j
public class KafkaTopicBootstrap {

    public KafkaTopicBootstrap(
            final Map<String, Object> kafkaAdminClient, final NewTopic topic, final long topicCreationTimeoutMs) {
        final AdminClient adminClient = KafkaAdminClient.create(kafkaAdminClient);
        ShutdownHook.add("Topics Creator", adminClient);

        try {
            adminClient.createTopics(List.of(topic)).all().get(topicCreationTimeoutMs, TimeUnit.MILLISECONDS);
            log.info("Topics {} created", topic);
        } catch (Exception e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.warn(e.getMessage());
            } else {
                throw new CanaryException(e);
            }
        }
    }
}
