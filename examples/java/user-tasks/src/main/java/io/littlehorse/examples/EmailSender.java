package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;

public class EmailSender {

    @LHTaskMethod("send-email")
    public void sendEmail(
        String address,
        String content,
        WorkerContext workerContext
    ) {
        if (workerContext.getUserId() != null) {
            System.out.println(
                "Received variable by " + workerContext.getUserId()
            );
        } else if (workerContext.getUserGroup() != null) {
            System.out.println(
                "Received variable by " + workerContext.getUserGroup()
            );
        }

        System.out.println("\n\nSending email to " + address);
        System.out.println("Content: " + content);
    }
}
