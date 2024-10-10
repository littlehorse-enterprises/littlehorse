package io.littlehorse.server.streams;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

@Slf4j
public class TaskClaimEventProducerCallback implements Callback {

    private final ScheduledTaskModel scheduledTask;
    private final PollTaskRequestObserver client;

    public TaskClaimEventProducerCallback(ScheduledTaskModel scheduledTask, PollTaskRequestObserver client) {
        this.scheduledTask = scheduledTask;
        this.client = client;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            client.sendResponse(scheduledTask);
        } else {
            log.error("error", exception);
        }
    }
}
