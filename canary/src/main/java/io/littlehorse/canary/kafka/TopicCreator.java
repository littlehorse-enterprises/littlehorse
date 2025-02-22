package io.littlehorse.canary.kafka;

import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.infra.ShutdownHook;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;

@Slf4j
public class TopicCreator {

    public TopicCreator(
            final Map<String, Object> kafkaAdminClient, final Duration timeout, final List<NewTopic> topics) {
        final AdminClient adminClient = KafkaAdminClient.create(kafkaAdminClient);
        ShutdownHook.add("Topics Creator", adminClient);

        try {
            adminClient.createTopics(topics).all().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            log.info("Topics {} created", topics);
        } catch (Exception e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.warn(e.getMessage());
            } else {
                throw new CanaryException(e);
            }
        }
    }
}
