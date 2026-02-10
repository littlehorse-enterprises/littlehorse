package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
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

    private static final Logger log = LoggerFactory.getLogger(ParallelApprovalExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("parallel-approval", wf -> {
            // Initialize variables.
            WfRunVariable person1Approved = wf.declareBool("person-1-approved");
            WfRunVariable person2Approved = wf.declareBool("person-2-approved");
            WfRunVariable person3Approved = wf.declareBool("person-3-approved");
            WfRunVariable allApproved = wf.declareBool("all-approved");

            // Variables are initialized to NULL. Need to set to a real value.
            allApproved.assign(false);
            person1Approved.assign(false);
            person2Approved.assign(false);
            person3Approved.assign(false);

            // Kick off the reminder workflow
            wf.spawnThread(sendReminders(allApproved), "send-reminders", null);

            // Wait for all users to approve the transaction
            SpawnedThread p1Thread = wf.spawnThread(waitForPerson1(person1Approved), "person-1", null);
            SpawnedThread p2Thread = wf.spawnThread(waitForPerson2(person2Approved), "person-2", null);
            SpawnedThread p3Thread = wf.spawnThread(waitForPerson3(person3Approved), "person-3", null);

            NodeOutput nodeOutput = wf.waitForThreads(SpawnedThreads.of(p1Thread, p2Thread, p3Thread));

            wf.handleException(nodeOutput, "denied-by-user", xnHandler -> {
                // HANDLE FAILED APPROVALS HERE.
                // If you want, you can execute additional business logic.

                xnHandler.fail("denied-by-user", "The workflow was not approved!");
            });

            // Tell the reminder workflow to stop
            allApproved.assign(true);
        });
    }

    private static ThreadFunc waitForPerson3(WfRunVariable person3Approved) {
        return approvalThread -> {
            WfRunVariable jsonVariable = approvalThread.declareJsonObj("person-3-response");
                jsonVariable.assign(approvalThread.waitForEvent("person-3-approves"));
            approvalThread
                    .doIf(
                            approvalThread.condition(jsonVariable.jsonPath("$.approval"), Comparator.EQUALS, true),
                            ifHandler -> {
                                person3Approved.assign(true);
                            })
                    .doElse(elseHandler -> {
                        approvalThread.fail("denied-by-user", "message here");
                    });
        };
    }

    private static ThreadFunc waitForPerson2(WfRunVariable person2Approved) {
        return approvalThread -> {
            WfRunVariable jsonVariable = approvalThread.declareJsonObj("person-2-response");
                jsonVariable.assign(approvalThread.waitForEvent("person-2-approves"));
            approvalThread
                    .doIf(
                            approvalThread.condition(jsonVariable.jsonPath("$.approval"), Comparator.EQUALS, true),
                            ifHandler -> {
                                person2Approved.assign(true);
                            })
                    .doElse(elseHandler -> {
                        approvalThread.fail("denied-by-user", "message here");
                    });
        };
    }

    private static ThreadFunc waitForPerson1(WfRunVariable person1Approved) {
        return approvalThread -> {
            WfRunVariable jsonVariable = approvalThread.declareJsonObj("person-1-response");
                jsonVariable.assign(approvalThread.waitForEvent("person-1-approves"));
            approvalThread
                    .doIf(
                            approvalThread.condition(jsonVariable.jsonPath("$.approval"), Comparator.EQUALS, true),
                            ifHandler -> {
                                person1Approved.assign(true);
                            })
                    .doElse(elseHandler -> {
                        approvalThread.fail("denied-by-user", "message here");
                    });
        };
    }

    private static ThreadFunc sendReminders(WfRunVariable allApproved) {
        return reminderThread -> {
            WfRunVariable nextReminderTime = reminderThread.declareInt("next-reminder");

            // Calculate next time to send notification
                nextReminderTime.assign(reminderThread.execute("calculate-next-notification"));

            reminderThread.sleepUntil(nextReminderTime);

            // So long as all things haven't been approved yet, continue to send reminders.
            reminderThread.doWhile(reminderThread.condition(allApproved, Comparator.EQUALS, false), loop -> {
                reminderThread.execute("reminder-task");

                // Calculate next reminder
                nextReminderTime.assign(reminderThread.execute("calculate-next-notification"));

                // Wait until next reminder
                reminderThread.sleepUntil(nextReminderTime);
            });
        };
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

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        Notifier executable = new Notifier();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "calculate-next-notification", config),
                new LHTaskWorker(executable, "reminder-task", config),
                new LHTaskWorker(executable, "exc-handler", config));

        // Gracefully shutdown
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New workers
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register tasks
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register external event
        Set<String> externalEventNames = workflow.getRequiredExternalEventDefNames();

        for (String externalEventName : externalEventNames) {
            log.debug("Registering external event {}", externalEventName);
            client.putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                    .setName(externalEventName)
                    .build());
        }

        // Register a workflow
        workflow.registerWfSpec(client);

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
