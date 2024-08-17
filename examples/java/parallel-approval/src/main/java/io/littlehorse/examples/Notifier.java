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
        return Instant.now().plus(20, ChronoUnit.SECONDS).toEpochMilli();
    }

    @LHTaskMethod("reminder-task")
    public String reminderTask() {
        System.out.println("\n\n\n\n******\nreminder-task!!!\n******\n\n\n");
        return "I just sent a reminder!";
    }

    @LHTaskMethod("exc-handler")
    public String handler(){
        System.out.println("Ok, handler was called by exception handler");
        return "Ok?";
    }
}
