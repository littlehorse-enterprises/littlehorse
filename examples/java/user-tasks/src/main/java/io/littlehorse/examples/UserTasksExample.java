package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.wfsdk.*;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class UserTasksExample {

    private static final String WF_NAME = "it-request";
    public static final String EMAIL_TASK_NAME = "send-email";

    private static final String IT_REQUEST_FORM = "it-request";
    public static final String APPROVAL_FORM = "approve-it-request";

    public Workflow getWorkflow() {
        return new WorkflowImpl(WF_NAME, this::wf);
    }

    public void wf(WorkflowThread wf) {
        WfRunVariable userId = wf.declareStr("user-id");
        WfRunVariable itRequest = wf.declareStruct("it-request", ItemRequestForm.class);
        WfRunVariable isApproved = wf.declareBool("is-approved");

        // Get the IT Request
        UserTaskOutput formOutput = wf.assignUserTask(IT_REQUEST_FORM, userId, "testGroup");
        wf.releaseToGroupOnDeadline(formOutput, 60);

        wf.handleException(formOutput, handler -> {
            String email = "test-ut-support@gmail.com";
            handler.execute(EMAIL_TASK_NAME, email, "Task cancelled");
        });
        itRequest.assign(formOutput);

        // Have Finance approve the request
        UserTaskOutput financeUserTaskOutput = wf.assignUserTask(APPROVAL_FORM, null, "finance")
                .withNotes(wf.format(
                        "User {0} is requesting to buy item {1}.\nJustification: {2}",
                        userId, itRequest.get("requestedItem"), itRequest.get("justification")));
        String financeTeamEmailBody = "Hi finance team, you have a new assigned task";
        String financeTeamEmail = "finance@gmail.com";
        wf.scheduleReminderTask(financeUserTaskOutput, 2, EMAIL_TASK_NAME, financeTeamEmail, financeTeamEmailBody);
        wf.reassignUserTask(financeUserTaskOutput, "test-eduwer", null, 60);

        isApproved.assign(financeUserTaskOutput.get("approved"));

        wf.doIf(
                        isApproved.isEqualTo(true),
                        // Request approved!
                        ifBody -> {
                            ifBody.execute(
                                    EMAIL_TASK_NAME,
                                    userId,
                                    wf.format(
                                            "Dear {0}, your request for {1} has been approved!",
                                            userId, itRequest.get("requestedItem")));
                        })
                .doElse(
                        // Request denied ):
                        elseBody -> {
                            elseBody.execute(
                                    EMAIL_TASK_NAME,
                                    userId,
                                    wf.format(
                                            "Dear {0}, your request for {1} has been denied.",
                                            userId, itRequest.get("requestedItem")));
                        });
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public LHTaskWorker getTaskWorker(LHConfig config) {
        EmailSender executable = new EmailSender();
        LHTaskWorker worker = new LHTaskWorker(executable, "send-email", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        new UserTasksExample().doMain();
    }

    public void doMain() throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

        StructDef itemRequestFormStructDef = registerStructDef(client, ItemRequestForm.class);
        StructDef approvalFormStructDef = registerStructDef(client, ApprovalForm.class);

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        LHTaskWorker worker = getTaskWorker(config);
        worker.registerTaskDef();

        // Create the User Task Def
        PutUserTaskDefRequest requestForm = PutUserTaskDefRequest.newBuilder()
                .setName(IT_REQUEST_FORM)
                .setResultStructDefId(itemRequestFormStructDef.getId())
                .build();
        client.putUserTaskDef(requestForm);

        PutUserTaskDefRequest approvalForm = PutUserTaskDefRequest.newBuilder()
                .setName(APPROVAL_FORM)
                .setResultStructDefId(approvalFormStructDef.getId())
                .build();
        client.putUserTaskDef(approvalForm);

        workflow.registerWfSpec(client);

        // Run the worker
        worker.start();
    }

    private static StructDef registerStructDef(
            LittleHorseGrpc.LittleHorseBlockingStub client, Class<?> structDefClass) {
        StructDefCompatibilityType compatibilityType = StructDefCompatibilityType.NO_SCHEMA_UPDATES;

        LHStructDefType structDefType = new LHStructDefType(structDefClass);

        PutStructDefRequest request = structDefType.toPutStructDefRequest().toBuilder()
                .setAllowedUpdates(compatibilityType)
                .build();

        return client.putStructDef(request);
    }
}
