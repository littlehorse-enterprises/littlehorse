package io.littlehorse.examples;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.wfsdk.ThreadBuilder;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTasksExample {

    private static final Logger log = LoggerFactory.getLogger(UserTasksExample.class);

    private static final String WF_NAME = "it-request";
    public static final String EMAIL_TASK_NAME = "send-email";

    private static final String IT_REQUEST_FORM = "it-request";
    public static final String APPROVAL_FORM = "approve-it-request";

    public Workflow getWorkflow() {
        return new WorkflowImpl(WF_NAME, this::wf);
    }

    public void wf(ThreadBuilder thread) {
        WfRunVariable userId = thread.addVariable("user-id", VariableTypePb.STR);
        WfRunVariable itRequest = thread.addVariable(
            "it-request",
            VariableTypePb.JSON_OBJ
        );
        WfRunVariable isApproved = thread.addVariable(
            "is-approved",
            VariableTypePb.BOOL
        );

        // Get the IT Request
        UserTaskOutput formOutput = thread.assignUserTaskToUser(
            IT_REQUEST_FORM,
            userId
        );
        thread.mutate(itRequest, VariableMutationTypePb.ASSIGN, formOutput);

        // Have Finance approve the request
        UserTaskOutput financeUserTaskOutput = thread
            .assignUserTaskToUserGroup(APPROVAL_FORM, "finance")
            .withNotes(
                thread.format(
                    "User {0} is requesting to buy item {1}.\nJustification: {2}",
                    userId,
                    itRequest.jsonPath("$.requestedItem"),
                    itRequest.jsonPath("$.justification")
                )
            );
        String financeTeamEmailBody = "Hi finance team, you have a new assigned task";
        String financeTeamEmail = "finance@gmail.com";
        thread.scheduleTaskAfter(
            financeUserTaskOutput,
            2,
            EMAIL_TASK_NAME,
            financeTeamEmail,
            financeTeamEmailBody
        );

        thread.mutate(
            isApproved,
            VariableMutationTypePb.ASSIGN,
            financeUserTaskOutput.jsonPath("$.isApproved")
        );

        thread.doIfElse(
            thread.condition(isApproved, ComparatorPb.EQUALS, true),
            // Request approved!
            ifBody -> {
                ifBody.execute(
                    EMAIL_TASK_NAME,
                    userId,
                    thread.format(
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
                    thread.format(
                        "Dear {0}, your request for {1} has been denied.",
                        userId,
                        itRequest.jsonPath("$.requestedItem")
                    )
                );
            }
        );
    }

    public Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        Path configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        );
        props.load(new FileInputStream(configPath.toFile()));
        return props;
    }

    public LHTaskWorker getTaskWorker(LHWorkerConfig config) {
        EmailSender executable = new EmailSender();
        LHTaskWorker worker = new LHTaskWorker(executable, "send-email", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException, LHApiError {
        new UserTasksExample().doMain();
    }

    public void doMain() throws IOException, LHApiError {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHWorkerConfig config = new LHWorkerConfig(props);
        LHClient client = new LHClient(config);

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        LHTaskWorker worker = getTaskWorker(config);

        // Register task if it does not exist
        if (worker.doesTaskDefExist()) {
            log.warn(
                "Task {} already exists, skipping creation",
                worker.getTaskDefName()
            );
        } else {
            log.debug(
                "Task {} does not exist, registering it",
                worker.getTaskDefName()
            );
            worker.registerTaskDef();
        }

        // Create the User Task Def
        UserTaskSchema requestForm = new UserTaskSchema(
            new ItemRequestForm(),
            IT_REQUEST_FORM
        );
        client.putUserTaskDef(requestForm.compile(), true);
        UserTaskSchema approvalForm = new UserTaskSchema(
            new ApprovalForm(),
            APPROVAL_FORM
        );
        client.putUserTaskDef(approvalForm.compile(), true);

        // Register a workflow if it does not exist
        if (workflow.doesWfSpecExist(client)) {
            log.warn(
                "Workflow {} already exists, skipping creation",
                workflow.getName()
            );
        } else {
            log.debug(
                "Workflow {} does not exist, registering it",
                workflow.getName()
            );
            workflow.registerWfSpec(client);
        }

        // Run the worker
        worker.start();
    }
}
