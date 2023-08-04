package io.littlehorse.examples;

import io.littlehorse.sdk.common.proto.ScheduledTaskContextPb;
import io.littlehorse.sdk.common.proto.VarNameAndValPb;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import java.util.List;

public class EmailSender {

    @LHTaskMethod("send-email")
    public void sendEmail(
        String address,
        String content,
        WorkerContext workerContext
    ) {
        ScheduledTaskContextPb taskContext = workerContext.getTaskContext();
        if (taskContext != null) {
            List<VarNameAndValPb> contextVariables = taskContext.getVariablesList();
            for (VarNameAndValPb varNameAndValPb : contextVariables) {
                System.out.println(
                    "Received variable by " + varNameAndValPb.getVarName()
                );
                System.out.println(varNameAndValPb.getValue().getStr());
            }
        }

        System.out.println("\n\nSending email to " + address);
        System.out.println("Content: " + content);
    }
}
