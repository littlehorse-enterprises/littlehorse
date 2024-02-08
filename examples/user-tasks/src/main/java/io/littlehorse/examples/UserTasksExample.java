package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
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
        WfRunVariable userId = wf.addVariable("user-id", VariableType.STR);
        WfRunVariable itRequest = wf.addVariable(
            "it-request",
            VariableType.JSON_OBJ
        );
        WfRunVariable isApproved = wf.addVariable(
            "is-approved",
            VariableType.BOOL
        );

        // Get the IT Request
        UserTaskOutput formOutput = wf.assignUserTask(
            IT_REQUEST_FORM,
            userId,
            "testGroup"
        );

        wf.handleException(
            formOutput,
            "USER_TASK_CANCELLED",
            handler -> {
                String email = "test-ut-support@gmail.com";
                handler.execute(EMAIL_TASK_NAME, email, "Task cancelled");
            }
        );
        wf.mutate(itRequest, VariableMutationType.ASSIGN, formOutput);

        // Have Finance approve the request
        UserTaskOutput financeUserTaskOutput = wf
            .assignUserTask(APPROVAL_FORM, null, "finance")
            .withNotes(
                wf.format(
                    "User {0} is requesting to buy item {1}.\nJustification: {2}",
                    userId,
                    itRequest.jsonPath("$.requestedItem"),
                    itRequest.jsonPath("$.justification")
                )
            );
        String financeTeamEmailBody = "Hi finance team, you have a new assigned task";
        String financeTeamEmail = "finance@gmail.com";
        wf.scheduleReminderTask(
            financeUserTaskOutput,
            2,
            EMAIL_TASK_NAME,
            financeTeamEmail,
            financeTeamEmailBody
        );
        wf.reassignUserTask(
            financeUserTaskOutput,
            "test-eduwer",
            null,
            60
        );

        wf.mutate(
            isApproved,
            VariableMutationType.ASSIGN,
            financeUserTaskOutput.jsonPath("$.isApproved")
        );

        wf.doIfElse(
            wf.condition(isApproved, Comparator.EQUALS, true),
            // Request approved!
            ifBody -> {
                ifBody.execute(
                    EMAIL_TASK_NAME,
                    userId,
                    wf.format(
                        "Dear {0}, your request for {1} has been approved!",
                        userId,
                        itRequest.jsonPath("$.requestedItem")
                    )
                );
            },
            // Request denied ):
            elseBody -> {
                elseBody.execute(
                    EMAIL_TASK_NAME,
                    userId,
                    wf.format(
                        "Dear {0}, your request for {1} has been denied.",
                        userId,
                        itRequest.jsonPath("$.requestedItem")
                    )
                );
            }
        );
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        ).toFile();
        if(configPath.exists()){
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

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        LHTaskWorker worker = getTaskWorker(config);
        worker.registerTaskDef();

        // Create the User Task Def
        UserTaskSchema requestForm = new UserTaskSchema(
            new ItemRequestForm(),
            IT_REQUEST_FORM
        );
        client.putUserTaskDef(requestForm.compile());

        UserTaskSchema approvalForm = new UserTaskSchema(
            new ApprovalForm(),
            APPROVAL_FORM
        );
        client.putUserTaskDef(approvalForm.compile());

        workflow.registerWfSpec(client);

        // Run the worker
        worker.start();
    }
}
