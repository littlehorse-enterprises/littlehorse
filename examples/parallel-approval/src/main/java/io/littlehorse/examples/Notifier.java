package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notifier {

    private static final Logger log = LoggerFactory.getLogger(Notifier.class);

    @LHTaskMethod("calculate-next-notification")
    public long calculateNextNotification() {
        log.debug("Executing calculate-next-notification");
        return Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();
    }

    @LHTaskMethod("reminder-task")
    public String reminderTask() {
        log.debug("reminder-task");
        return "I just sent a reminder!";
    }
}
