package io.littlehorse.canary.kafka;

import io.littlehorse.canary.app.BoostrapInitializationException;
import io.littlehorse.canary.app.Bootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;

@Slf4j
public class KafkaBootstrap implements Bootstrap {

    private AdminClient adminClient;

    @Override
    public void initialize(CanaryConfig config) throws BoostrapInitializationException {
        adminClient = KafkaAdminClient.create(config.toKafkaAdminConfig().toMap());

        try {
            adminClient
                    .createTopics(List.of(new NewTopic(
                            config.getTopicName(), config.getTopicPartitions(), config.getTopicReplicas())))
                    .all()
                    .get();
            log.info("Topics {} created", config.getTopicName());
        } catch (Exception e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.warn(e.getMessage());
                return;
            }
            throw new BoostrapInitializationException(e);
        }
        log.info("Initialized");
    }

    @Override
    public void shutdown() {
        if (adminClient != null) {
            adminClient.close();
        }
        log.info("Shutdown");
    }
}
