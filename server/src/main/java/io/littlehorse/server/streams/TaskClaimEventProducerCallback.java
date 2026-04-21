package io.littlehorse.server.streams;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

public class TaskClaimEventProducerCallback implements Callback {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskClaimEventProducerCallback.class);
    private final ScheduledTaskModel scheduledTask;
    private final PollTaskRequestObserver client;

    public TaskClaimEventProducerCallback(
            final ScheduledTaskModel scheduledTask, final PollTaskRequestObserver client) {
        this.scheduledTask = scheduledTask;
        this.client = client;
    }

    @Override
    public void onCompletion(final RecordMetadata metadata, final Exception exception) {
        if (exception == null) {
            client.sendResponse(scheduledTask);
        } else {
            client.onError(exception);
            log.error("error", exception);
        }
    }
}
