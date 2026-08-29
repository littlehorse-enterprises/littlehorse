package io.littlehorse.sdkjs.golden;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.SpawnedChildWf;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Emits probe-pair fixtures: for each wfsdk feature, a minimal workflow
 * compiled twice by real sdk-java — once WITHOUT the feature (base) and once
 * WITH it (feature) — into sdk-js/golden/probes/NAME.base.json and
 * NAME.feature.json. The sdk-js twins (src/feature-matrix/harness/probes.ts)
 * must compile byte-identically to BOTH, and the two fixtures must differ
 * (else the probe is vacuous). See proposals/sdk-js-parity/wfsdk.md, Design 2.
 *
 * Authoring rules (the entire human contribution to the oracle):
 *  - same workflow name in both variants, so the diff is only the feature;
 *  - base is the feature's nearest do-nothing neighbor;
 *  - one feature per probe;
 *  - input maps carry at most ONE entry (Map.of has no iteration order).
 *
 * Regenerate with:
 *   ./gradlew :sdk-js-golden-generator:runProbes --args="$(pwd)/sdk-js/golden"
 */
public class ProbeGenerator {

    /** A probe pair: compile(false) = base, compile(true) = base + feature. */
    private static final Map<String, Function<Boolean, Workflow>> PAIRS = new LinkedHashMap<>();

    /**
     * Single-fixture probes, for capabilities that ARE the precondition of any
     * output and therefore cannot be toggled (no sensitivity assertion).
     */
    private static final Map<String, Workflow> SINGLES = new LinkedHashMap<>();

    private static Workflow probe(String name, ThreadFunc entrypoint) {
        return new WorkflowImpl("probe-" + name, entrypoint);
    }

    private static void pair(String name, Function<Boolean, ThreadFunc> threadFunc) {
        PAIRS.put(name, withFeature -> probe(name, threadFunc.apply(withFeature)));
    }

    static {
        // Workflow.newWorkflow + Workflow#compileWorkflow: creating and
        // compiling are what every fixture exercises; toggling them off yields
        // no output at all, so sensitivity is undefined — single fixture.
        SINGLES.put("workflow-minimal", probe("workflow-minimal", wf -> {}));

        // ---------------------------------------------------------- declares
        pair("declare-str", f -> wf -> {
            if (f) wf.declareStr("v");
        });
        pair("declare-int", f -> wf -> {
            if (f) wf.declareInt("v");
        });
        pair("declare-double", f -> wf -> {
            if (f) wf.declareDouble("v");
        });
        pair("declare-bool", f -> wf -> {
            if (f) wf.declareBool("v");
        });
        pair("declare-bytes", f -> wf -> {
            if (f) wf.declareBytes("v");
        });
        pair("declare-timestamp", f -> wf -> {
            if (f) wf.declareTimestamp("v");
        });
        pair("declare-json-obj", f -> wf -> {
            if (f) wf.declareJsonObj("v");
        });
        pair("declare-json-arr", f -> wf -> {
            if (f) wf.declareJsonArr("v");
        });

        // ------------------------------------------------- variable modifiers
        pair("var-with-default", f -> wf -> {
            WfRunVariable v = wf.declareInt("v");
            if (f) v.withDefault(42);
        });
        pair("var-required", f -> wf -> {
            WfRunVariable v = wf.declareStr("v");
            if (f) v.required();
        });
        pair("var-searchable", f -> wf -> {
            WfRunVariable v = wf.declareStr("v");
            if (f) v.searchable();
        });
        pair("var-searchable-on", f -> wf -> {
            WfRunVariable v = wf.declareJsonObj("v");
            if (f) v.searchableOn("$.customerId", VariableType.STR);
        });
        pair("var-masked", f -> wf -> {
            WfRunVariable v = wf.declareStr("v");
            if (f) v.masked();
        });
        pair("var-as-public", f -> wf -> {
            WfRunVariable v = wf.declareStr("v");
            if (f) v.asPublic();
        });
        pair("var-json-path", f -> wf -> {
            WfRunVariable v = wf.declareJsonObj("v");
            wf.execute("noop", f ? v.jsonPath("$.field") : v);
        });
        pair("var-assign", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(5);
        });

        // -------------------------------------------------------------- tasks
        pair("execute-args", f -> wf -> {
            WfRunVariable name = wf.declareStr("name");
            if (f) {
                wf.execute("greet", name);
            } else {
                wf.execute("greet");
            }
        });
        pair("task-with-retries", f -> wf -> {
            var node = wf.execute("noop");
            if (f) node.withRetries(3);
        });

        // ------------------------------------------------------- control flow
        // Shared by the condition() and doIf() entries: a condition only
        // manifests in output through a conditional consumer, so the nearest
        // isolatable feature is the conditioned branch itself.
        pair("do-if", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            wf.execute("noop");
            if (f) {
                wf.doIf(wf.condition(a, Comparator.GREATER_THAN, 5), body -> {
                    body.execute("branch");
                });
            }
        });
        pair("do-else-if", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            var ifStatement = wf.doIf(wf.condition(a, Comparator.GREATER_THAN, 10), body -> {
                body.execute("big");
            });
            if (f) {
                ifStatement.doElseIf(wf.condition(a, Comparator.GREATER_THAN, 5), body -> {
                    body.execute("medium");
                });
            }
        });
        pair("do-else", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            var ifStatement = wf.doIf(wf.condition(a, Comparator.GREATER_THAN, 10), body -> {
                body.execute("big");
            });
            if (f) {
                ifStatement.doElse(body -> {
                    body.execute("small");
                });
            }
        });
        pair("do-if-else", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            if (f) {
                wf.doIfElse(
                        wf.condition(a, Comparator.GREATER_THAN, 5),
                        body -> body.execute("yes"),
                        body -> body.execute("no"));
            }
        });
        pair("do-while", f -> wf -> {
            WfRunVariable remaining = wf.declareInt("remaining");
            if (f) {
                wf.doWhile(wf.condition(remaining, Comparator.GREATER_THAN, 0), body -> {
                    body.execute("process-one");
                });
            }
        });
        pair("fail", f -> wf -> {
            wf.execute("noop");
            if (f) wf.fail("business-problem", "Something went wrong");
        });

        // ------------------------------------------------------ sleep + waits
        pair("sleep-seconds", f -> wf -> {
            if (f) wf.sleepSeconds(30);
        });
        pair("sleep-until", f -> wf -> {
            WfRunVariable wakeAt = wf.declareTimestamp("wake-at");
            if (f) wf.sleepUntil(wakeAt);
        });
        pair("wait-for-condition", f -> wf -> {
            WfRunVariable ready = wf.declareBool("ready");
            if (f) wf.waitForCondition(wf.condition(ready, Comparator.EQUALS, true));
        });
        pair("throw-event", f -> wf -> {
            WfRunVariable payload = wf.declareStr("payload");
            if (f) wf.throwEvent("milestone", payload);
        });

        // -------------------------------------------------------- expressions
        pair("expr-add", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(count.add(1));
        });
        pair("expr-subtract", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(count.subtract(1));
        });
        pair("expr-multiply", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(count.multiply(2));
        });
        pair("expr-divide", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(count.divide(2));
        });
        pair("expr-remove-index", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            wf.execute("noop");
            if (f) items.assign(items.removeIndex(0));
        });
        pair("expr-size", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(items.size());
        });
        pair("mutate", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) wf.mutate(count, VariableMutationType.ADD, 5);
        });
        pair("format", f -> wf -> {
            WfRunVariable name = wf.declareStr("name");
            WfRunVariable label = wf.declareStr("label");
            wf.execute("noop");
            if (f) label.assign(wf.format("Hello {0}", name));
        });

        // ---------------------------------------------------- external events
        pair("wait-for-event", f -> wf -> {
            if (f) wf.waitForEvent("payment-received");
        });
        pair("external-event-timeout", f -> wf -> {
            var evt = wf.waitForEvent("payment-received");
            if (f) evt.timeout(3600);
        });
        pair("external-event-correlation", f -> wf -> {
            WfRunVariable orderId = wf.declareStr("order-id");
            var evt = wf.waitForEvent("payment-received");
            if (f) evt.withCorrelationId(orderId);
        });

        // ------------------------------------------------------ child threads
        pair("spawn-thread-input-vars", f -> wf -> {
            WfRunVariable amount = wf.declareInt("amount");
            wf.spawnThread(
                    child -> {
                        child.execute("child-task");
                    },
                    "child",
                    f ? Map.of("budget", amount) : Map.of());
        });
        pair("spawn-thread-for-each", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            if (f) {
                wf.spawnThreadForEach(items, "processor", child -> {
                    child.execute("process-one");
                });
            }
        });
        pair("wait-for-threads", f -> wf -> {
            SpawnedThread child = wf.spawnThread(
                    c -> {
                        c.execute("child-task");
                    },
                    "child",
                    Map.of());
            if (f) wf.waitForThreads(SpawnedThreads.of(child));
        });
        pair("spawned-threads-of", f -> wf -> {
            SpawnedThread first = wf.spawnThread(
                    c -> {
                        c.execute("child-a-task");
                    },
                    "child-a",
                    Map.of());
            SpawnedThread second = wf.spawnThread(
                    c -> {
                        c.execute("child-b-task");
                    },
                    "child-b",
                    Map.of());
            wf.waitForThreads(f ? SpawnedThreads.of(first, second) : SpawnedThreads.of(first));
        });

        // ---------------------------------------------------- child workflows
        pair("run-wf-inputs", f -> wf -> {
            WfRunVariable orderId = wf.declareStr("order-id");
            wf.runWf("child-wf", f ? Map.of("order-id", orderId) : Map.of());
        });
        pair("wait-for-child-wf", f -> wf -> {
            SpawnedChildWf child = wf.runWf("child-wf", Map.of());
            if (f) wf.waitForChildWf(child);
        });

        // --------------------------------------------------------- interrupts
        pair("interrupt-handler", f -> wf -> {
            wf.execute("main-step");
            if (f) {
                wf.registerInterruptHandler("cancel-requested", handler -> {
                    handler.execute("cancel");
                });
            }
        });

        // --------------------------------------------------- failure handling
        pair("handle-error-any", f -> wf -> {
            var node = wf.execute("risky-task");
            if (f) {
                wf.handleError(node, handler -> {
                    handler.execute("cleanup");
                });
            }
        });
        pair("handle-exception-named", f -> wf -> {
            var node = wf.execute("flaky-task");
            if (f) {
                wf.handleException(node, "out-of-stock", handler -> {
                    handler.execute("reorder");
                });
            }
        });
        pair("handle-any-failure", f -> wf -> {
            var node = wf.execute("fragile-task");
            if (f) {
                wf.handleAnyFailure(node, handler -> {
                    handler.execute("cleanup-any");
                });
            }
        });

        // --------------------------------------------------------- user tasks
        pair("assign-user-task", f -> wf -> {
            if (f) wf.assignUserTask("approve-request", "alice", "approvers");
        });
        pair("user-task-notes", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) ut.withNotes("Please review");
        });
        pair("release-to-group-on-deadline", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) wf.releaseToGroupOnDeadline(ut, 300);
        });
        pair("schedule-reminder-task", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) wf.scheduleReminderTask(ut, 60, "send-reminder");
        });
        pair("cancel-user-task-run-after", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) wf.cancelUserTaskRunAfter(ut, 86400);
        });
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: ProbeGenerator <golden-dir>");
            System.exit(1);
        }
        Path outputDir = Path.of(args[0]).resolve("probes");
        Files.createDirectories(outputDir);

        int files = 0;
        for (Map.Entry<String, Function<Boolean, Workflow>> entry : PAIRS.entrySet()) {
            write(outputDir, entry.getKey() + ".base", entry.getValue().apply(false));
            write(outputDir, entry.getKey() + ".feature", entry.getValue().apply(true));
            files += 2;
        }
        for (Map.Entry<String, Workflow> entry : SINGLES.entrySet()) {
            write(outputDir, entry.getKey(), entry.getValue());
            files++;
        }
        System.out.println("Generated " + files + " probe fixtures (" + PAIRS.size() + " pairs, "
                + SINGLES.size() + " singles) in " + outputDir);
    }

    private static void write(Path dir, String name, Workflow workflow) throws Exception {
        Files.writeString(dir.resolve(name + ".json"), workflow.compileWfToJson() + "\n", StandardCharsets.UTF_8);
    }
}
