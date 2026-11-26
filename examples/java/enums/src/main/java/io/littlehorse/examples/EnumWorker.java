package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumWorker {

    private static final Logger log = LoggerFactory.getLogger(EnumWorker.class);

    @LHTaskMethod("classify-priority")
    @LHType(isInlineEnum = true)
    public Priority classifyPriority(String summary) {
        String text = summary.toLowerCase();
        Priority priority;
        if (text.contains("outage") || text.contains("down")) {
            priority = Priority.CRITICAL;
        } else if (text.contains("error") || text.contains("fail")) {
            priority = Priority.HIGH;
        } else if (text.contains("slow")) {
            priority = Priority.MEDIUM;
        } else {
            priority = Priority.LOW;
        }
        log.info("Classified '{}' as {}", summary, priority);
        return priority;
    }

    @LHTaskMethod("page-on-call")
    public String pageOnCall(String summary, @LHType(isInlineEnum = true) Priority priority) {
        String message = "Paged on-call engineer for %s ticket: %s".formatted(priority, summary);
        log.info(message);
        return message;
    }

    @LHTaskMethod("add-to-queue")
    public String addToQueue(String summary, String priorityName) {
        String message = "Added %s ticket to the queue: %s".formatted(priorityName, summary);
        log.info(message);
        return message;
    }
}
