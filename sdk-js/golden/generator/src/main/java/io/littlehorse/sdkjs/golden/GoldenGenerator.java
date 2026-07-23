package io.littlehorse.sdkjs.golden;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.SpawnedChildWf;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates golden PutWfSpecRequest JSON files for the sdk-js parity harness
 * (see sdk-js/PARITY_PLAN.md). Each reference workflow exercises one area of
 * the wfsdk feature matrix; the JS wfsdk must compile the equivalent workflow
 * to the same proto.
 *
 * Regenerate with:
 *   ./gradlew :sdk-js-golden-generator:run --args="$(pwd)/sdk-js/golden"
 */
public class GoldenGenerator {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: GoldenGenerator <output-dir>");
            System.exit(1);
        }
        Path outputDir = Path.of(args[0]);
        Files.createDirectories(outputDir);

        Map<String, Workflow> workflows = new LinkedHashMap<>();
        workflows.put("basic", basic());
        workflows.put("variables", variables());
        workflows.put("conditionals", conditionals());
        workflows.put("expressions", expressions());
        workflows.put("while-loop", whileLoop());
        workflows.put("external-events", externalEvents());
        workflows.put("child-threads", childThreads());
        workflows.put("failure-handling", failureHandling());
        workflows.put("user-tasks", userTasks());
        workflows.put("sleep-and-events", sleepAndEvents());
        workflows.put("child-workflow", childWorkflow());
        workflows.put("interrupts", interrupts());

        for (Map.Entry<String, Workflow> entry : workflows.entrySet()) {
            Path out = outputDir.resolve(entry.getKey() + ".json");
            String json = entry.getValue().compileWfToJson();
            Files.writeString(out, json + "\n", StandardCharsets.UTF_8);
            System.out.println("Wrote " + out);
        }
        System.out.println("Generated " + workflows.size() + " golden files.");
    }

    /** Matrix: task nodes — execute by name with a variable arg, node output. */
    private static Workflow basic() {
        return new WorkflowImpl("golden-basic", wf -> {
            WfRunVariable name = wf.declareStr("name").required();
            wf.execute("greet", name);
        });
    }

    /** Matrix: variables — every declare* form plus modifiers. */
    private static Workflow variables() {
        return new WorkflowImpl("golden-variables", wf -> {
            wf.declareStr("my-str").required();
            wf.declareInt("my-int").withDefault(42);
            wf.declareDouble("my-double");
            wf.declareBool("my-bool");
            wf.declareBytes("my-bytes");
            wf.declareTimestamp("my-timestamp");
            wf.declareJsonObj("my-json-obj").searchableOn("$.customerId", VariableType.STR);
            wf.declareJsonArr("my-json-arr");
            wf.declareStr("my-searchable").searchable();
            wf.declareStr("my-masked").masked();
            wf.declareStr("my-public").asPublic();
            wf.execute("noop");
        });
    }

    /** Matrix: control flow — condition(), doIf/doElseIf/doElse, doIfElse, jsonPath. */
    private static Workflow conditionals() {
        return new WorkflowImpl("golden-conditionals", wf -> {
            WfRunVariable amount = wf.declareInt("amount");
            WfRunVariable customer = wf.declareJsonObj("customer");

            wf.doIf(wf.condition(amount, Comparator.GREATER_THAN, 100), body -> {
                        body.execute("large-order");
                    })
                    .doElseIf(wf.condition(amount, Comparator.GREATER_THAN, 10), body -> {
                        body.execute("medium-order");
                    })
                    .doElse(body -> {
                        body.execute("small-order");
                    });

            wf.doIfElse(
                    customer.jsonPath("$.isVip").isEqualTo(true),
                    body -> body.execute("vip-flow"),
                    body -> body.execute("regular-flow"));
        });
    }

    /** Matrix: expressions and mutations — arithmetic, collections, casts, mutate, format. */
    private static Workflow expressions() {
        return new WorkflowImpl("golden-expressions", wf -> {
            WfRunVariable count = wf.declareInt("count");
            WfRunVariable total = wf.declareDouble("total");
            WfRunVariable items = wf.declareJsonArr("items");
            WfRunVariable label = wf.declareStr("label");

            count.assign(count.add(1));
            total.assign(count.multiply(2).divide(4).subtract(1));
            items.assign(items.removeIndex(0));
            label.assign(wf.format("count is {0} of {1}", count, total));
            count.assign(items.size());
            total.assign(count.castToDouble());
            wf.mutate(count, VariableMutationType.ADD, 5);
            wf.execute("noop");
        });
    }

    /** Matrix: control flow — doWhile. */
    private static Workflow whileLoop() {
        return new WorkflowImpl("golden-while-loop", wf -> {
            WfRunVariable remaining = wf.declareInt("remaining");
            wf.doWhile(wf.condition(remaining, Comparator.GREATER_THAN, 0), body -> {
                body.execute("process-one");
                body.mutate(remaining, VariableMutationType.SUBTRACT, 1);
            });
        });
    }

    /** Matrix: external events — waitForEvent, timeout, correlation. */
    private static Workflow externalEvents() {
        return new WorkflowImpl("golden-external-events", wf -> {
            WfRunVariable orderId = wf.declareStr("order-id");
            WfRunVariable payment = wf.declareJsonObj("payment");

            payment.assign(wf.waitForEvent("payment-received").timeout(3600).withCorrelationId(orderId));
            wf.execute("fulfill", orderId, payment);
        });
    }

    /** Matrix: child threads — spawnThread, spawnThreadForEach, waitFor variants. */
    private static Workflow childThreads() {
        return new WorkflowImpl("golden-child-threads", wf -> {
            WfRunVariable orders = wf.declareJsonArr("orders");

            SpawnedThread notifier = wf.spawnThread(
                    child -> {
                        child.execute("notify");
                    },
                    "notifier",
                    Map.of());
            SpawnedThread auditor = wf.spawnThread(
                    child -> {
                        child.execute("audit");
                    },
                    "auditor",
                    Map.of());
            wf.waitForThreads(SpawnedThreads.of(notifier, auditor));

            SpawnedThreads processors = wf.spawnThreadForEach(orders, "processor", child -> {
                child.execute("process-order");
            });
            wf.waitForThreads(processors);
        });
    }

    /** Matrix: failure handling — handleError, handleException, handleAnyFailure, fail. */
    private static Workflow failureHandling() {
        return new WorkflowImpl("golden-failure-handling", wf -> {
            var risky = wf.execute("risky-task");
            wf.handleError(risky, handler -> {
                handler.execute("cleanup-error");
            });

            var flaky = wf.execute("flaky-task").withRetries(3);
            wf.handleException(flaky, "out-of-stock", handler -> {
                handler.execute("reorder");
            });

            var fragile = wf.execute("fragile-task");
            wf.handleAnyFailure(fragile, handler -> {
                handler.execute("cleanup-any");
                handler.fail("unrecoverable", "Could not recover from failure");
            });
        });
    }

    /** Matrix: user tasks — assignment, notes, reassignment, reminders, cancellation. */
    private static Workflow userTasks() {
        return new WorkflowImpl("golden-user-tasks", wf -> {
            WfRunVariable approver = wf.declareStr("approver");

            // releaseToGroupOnDeadline requires the task to be assigned to a
            // user AND a group (IllegalStateException otherwise) — matrix note.
            UserTaskOutput approval = wf.assignUserTask("approve-request", approver, "approvers")
                    .withNotes(wf.format("Approval needed from {0}", approver));
            wf.releaseToGroupOnDeadline(approval, 300);
            wf.scheduleReminderTask(approval, 60, "send-reminder");
            wf.cancelUserTaskRunAfter(approval, 86400);
            wf.execute("finalize");
        });
    }

    /** Matrix: control flow + workflow events — sleep, waitForCondition, throwEvent, complete. */
    private static Workflow sleepAndEvents() {
        return new WorkflowImpl("golden-sleep-and-events", wf -> {
            WfRunVariable ready = wf.declareBool("ready");
            WfRunVariable wakeAt = wf.declareTimestamp("wake-at");

            wf.sleepSeconds(30);
            wf.sleepUntil(wakeAt);
            wf.waitForCondition(wf.condition(ready, Comparator.EQUALS, true));
            wf.throwEvent("milestone-reached", ready);
            wf.complete();
        });
    }

    /** Matrix: child workflows — runWf, waitForChildWf. */
    private static Workflow childWorkflow() {
        return new WorkflowImpl("golden-child-workflow", wf -> {
            WfRunVariable orderId = wf.declareStr("order-id");
            SpawnedChildWf shipping = wf.runWf("shipping-wf", Map.of("order-id", orderId));
            wf.waitForChildWf(shipping);
        });
    }

    /** Matrix: interrupts — registerInterruptHandler. */
    private static Workflow interrupts() {
        return new WorkflowImpl("golden-interrupts", wf -> {
            wf.registerInterruptHandler("cancel-requested", handler -> {
                handler.execute("cancel-order");
            });
            wf.execute("long-running-step");
        });
    }
}
