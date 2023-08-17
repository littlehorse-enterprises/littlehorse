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
        if (workerContext.getUser() != null) {
            System.out.println(
                "Received variable by " + workerContext.getUser().getId()
            );
        } else if (workerContext.getUserGroup() != null) {
            System.out.println(
                "Received variable by " + workerContext.getUserGroup().getId()
            );
        }

        System.out.println("\n\nSending email to " + address);
        System.out.println("Content: " + content);
    }
}
