package io.littlehorse.examples;

import io.littlehorse.sdk.common.proto.UserPb;
import io.littlehorse.sdk.common.proto.UserTaskTriggerContextPb;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;

public class EmailSender {

    @LHTaskMethod("send-email")
    public void sendEmail(
        String address,
        String content,
        WorkerContext workerContext
    ) {
        UserTaskTriggerContextPb taskContext = null;
        if (taskContext != null) {
            UserPb user = taskContext.getUser();
            System.out.println("Received variable by " + user.getId());
        }

        System.out.println("\n\nSending email to " + address);
        System.out.println("Content: " + content);
    }
}
