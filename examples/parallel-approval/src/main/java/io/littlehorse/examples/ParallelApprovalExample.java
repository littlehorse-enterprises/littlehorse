package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * In this example you will see the poser of the thread.waitForThreads feature.
 * When multiples thread are running in parallel and you need to wait for all of them to finish,
 * it is possible to use thread.waitForThreads.
 */
public class ParallelApprovalExample {

    private static final Logger log = LoggerFactory.getLogger(
        ParallelApprovalExample.class
    );

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "parallel-approval-with-handler",
            thread -> {
                // Initialize variables.
                WfRunVariable person1Approved = thread.addVariable(
                    "person-1-approved",
                    VariableType.BOOL
                );
                WfRunVariable person2Approved = thread.addVariable(
                    "person-2-approved",
                    VariableType.BOOL
                );
                WfRunVariable person3Approved = thread.addVariable(
                    "person-3-approved",
                    VariableType.BOOL
                );
                WfRunVariable allApproved = thread.addVariable(
                    "all-approved",
                    VariableType.BOOL
                );

                // Variables are initialized to NULL. Need to set to a real value.
                thread.mutate(allApproved, VariableMutationType.ASSIGN, false);
                thread.mutate(person1Approved, VariableMutationType.ASSIGN, false);
                thread.mutate(person2Approved, VariableMutationType.ASSIGN, false);
                thread.mutate(person3Approved, VariableMutationType.ASSIGN, false);

                // Kick off the reminder workflow
                thread.spawnThread(
                    sendReminders(allApproved),
                    "send-reminders",
                    null
                );

                // Wait for all users to approve the transaction
                SpawnedThread p1Thread = thread.spawnThread(
                    waitForPerson1(person1Approved),
                    "person-1",
                    null
                );
                SpawnedThread p2Thread = thread.spawnThread(
                    waitForPerson2(person2Approved),
                    "person-2",
                    null
                );
                SpawnedThread p3Thread = thread.spawnThread(
                    waitForPerson3(person3Approved),
                    "person-3",
                    null
                );

                NodeOutput nodeOutput = thread.waitForThreads(p1Thread, p2Thread, p3Thread);

                thread.handleException(nodeOutput, "denied-by-user", xnHandler -> {
                    xnHandler.execute("exc-handler");
                });

                // Tell the reminder workflow to stop
                thread.mutate(allApproved, VariableMutationType.ASSIGN, true);
            }
        );
    }

    private static ThreadFunc waitForPerson3(WfRunVariable person3Approved) {
        return approvalThread -> {
            WfRunVariable jsonVariable = approvalThread.addVariable("person-3-response", VariableType.JSON_OBJ);
            approvalThread.mutate(jsonVariable, VariableMutationType.ASSIGN, approvalThread.waitForEvent("person-3-approves"));
            approvalThread.doIfElse(
                    approvalThread.condition(jsonVariable.jsonPath("$.approval"), Comparator.EQUALS, true),
                    ifHandler -> {
                        approvalThread.mutate(person3Approved, VariableMutationType.ASSIGN, true);
                    },
                    elseHandler -> {
                        approvalThread.fail("denied-by-user", "message here");
                    });
        };
    }

    private static ThreadFunc waitForPerson2(WfRunVariable person2Approved) {
        return approvalThread -> {
            WfRunVariable jsonVariable = approvalThread.addVariable("person-2-response", VariableType.JSON_OBJ);
            approvalThread.mutate(jsonVariable, VariableMutationType.ASSIGN, approvalThread.waitForEvent("person-2-approves"));
            approvalThread.doIfElse(
                    approvalThread.condition(jsonVariable.jsonPath("$.approval"), Comparator.EQUALS, true),
                    ifHandler -> {
                        approvalThread.mutate(person2Approved, VariableMutationType.ASSIGN, true);
                    },
                    elseHandler -> {
                        approvalThread.fail("denied-by-user", "message here");
                    });
        };
    }

    private static ThreadFunc waitForPerson1(WfRunVariable person1Approved) {
        return approvalThread -> {
            WfRunVariable jsonVariable = approvalThread.addVariable("person-1-response", VariableType.JSON_OBJ);
            approvalThread.mutate(jsonVariable, VariableMutationType.ASSIGN, approvalThread.waitForEvent("person-1-approves"));
            approvalThread.doIfElse(
                    approvalThread.condition(jsonVariable.jsonPath("$.approval"), Comparator.EQUALS, true),
                    ifHandler -> {
                        approvalThread.mutate(person1Approved, VariableMutationType.ASSIGN, true);
                    },
                    elseHandler -> {
                        approvalThread.fail("denied-by-user", "message here");
                    });

        };
    }

    private static ThreadFunc sendReminders(WfRunVariable allApproved) {
        return reminderThread -> {
            WfRunVariable nextReminderTime = reminderThread.addVariable(
                "next-reminder",
                VariableType.INT
            );

            // Calculate next time to send notification
            reminderThread.mutate(
                nextReminderTime,
                VariableMutationType.ASSIGN,
                reminderThread.execute("calculate-next-notification")
            );

            reminderThread.sleepUntil(nextReminderTime);

            // So long as all things haven't been approved yet, continue to send reminders.
            reminderThread.doWhile(
                reminderThread.condition(allApproved, Comparator.EQUALS, false),
                loop -> {
                    reminderThread.execute("reminder-task");

                    // Calculate next reminder
                    reminderThread.mutate(
                        nextReminderTime,
                        VariableMutationType.ASSIGN,
                        reminderThread.execute("calculate-next-notification")
                    );

                    // Wait until next reminder
                    reminderThread.sleepUntil(nextReminderTime);
                }
            );
        };
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        Path configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        );
        props.load(new FileInputStream(configPath.toFile()));
        return props;
    }

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) throws IOException {
        Notifier executable = new Notifier();
        List<LHTaskWorker> workers = List.of(
            new LHTaskWorker(executable, "calculate-next-notification", config),
            new LHTaskWorker(executable, "reminder-task", config),
            new LHTaskWorker(executable, "exc-handler", config)
        );

        // Gracefully shutdown
        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() ->
                    workers.forEach(worker -> {
                        log.debug("Closing {}", worker.getTaskDefName());
                        worker.close();
                    })
                )
            );
        return workers;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LHPublicApiGrpc.LHPublicApiBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New workers
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register tasks if they don't exist
        for (LHTaskWorker worker : workers) {
            if (worker.doesTaskDefExist()) {
                log.debug(
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
        }

        // Register external event if it does not exist
        Set<String> externalEventNames = workflow.getRequiredExternalEventDefNames();

        for (String externalEventName : externalEventNames) {
            log.debug("Registering external event {}", externalEventName);
            try{
                client.putExternalEventDef(
                        PutExternalEventDefRequest
                                .newBuilder()
                                .setName(externalEventName)
                                .build()
                );
            }catch (Exception e){}

        }

        // Register a workflow if it does not exist
        if (workflow.doesWfSpecExist(client)) {
            log.debug(
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

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
