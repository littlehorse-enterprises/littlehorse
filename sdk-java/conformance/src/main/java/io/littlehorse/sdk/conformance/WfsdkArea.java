package io.littlehorse.sdk.conformance;

import io.littlehorse.sdk.common.proto.AllowedUpdateType;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
import io.littlehorse.sdk.wfsdk.SpawnedChildWf;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * wfsdk case definitions + the exam's `compile` answers. WfsdkAreaMint
 * mints from these same definitions, so answers and canon cannot drift.
 * Recipe: sdk-conformance/areas/wfsdk/rules.md. Authoring rules: same workflow
 * name in both variants; base is the feature's nearest do-nothing
 * neighbor; one feature per case; input maps carry at most ONE entry
 * (Map.of has no iteration order).
 */
public final class WfsdkArea {

    /** Pair cases: compile(false) = base variant, compile(true) = feature variant. */
    private static final Map<String, Function<Boolean, Workflow>> PAIRS = new LinkedHashMap<>();

    /**
     * Single-variant: capabilities that are the precondition of any output,
     * so no base exists (proposals/sdk-conformance/design.md).
     */
    private static final Map<String, Workflow> SINGLES = new LinkedHashMap<>();

    private WfsdkArea() {}

    private static void pair(String caseId, Function<Boolean, ThreadFunc> body) {
        PAIRS.put(caseId, withFeature -> Workflow.newWorkflow("probe-" + caseId, body.apply(withFeature)));
    }

    /** For Workflow-level features: the body builds the whole Workflow. */
    private static void pairWf(String caseId, Function<Boolean, Workflow> body) {
        PAIRS.put(caseId, body);
    }

    private static void single(String caseId, ThreadFunc body) {
        SINGLES.put(caseId, Workflow.newWorkflow("probe-" + caseId, body));
    }

    static {
        single("workflow-minimal", wf -> {});

        // declares
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
        pair("declare-array", f -> wf -> {
            if (f) wf.declareArray("v", String.class);
        });
        pair("declare-map", f -> wf -> {
            if (f) wf.declareMap("v", String.class, String.class);
        });
        pair("declare-struct", f -> wf -> {
            if (f) wf.declareStruct("v", "customer");
        });
        pair("add-variable", f -> wf -> {
            if (f) wf.addVariable("v", VariableType.STR);
        });

        // variable modifiers
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
        pair("var-with-access-level", f -> wf -> {
            WfRunVariable v = wf.declareStr("v");
            if (f) v.withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR);
        });
        pair("var-as-inherited", f -> wf -> {
            WfRunVariable v = wf.declareStr("v");
            if (f) v.asInherited();
        });
        pair("var-json-path", f -> wf -> {
            WfRunVariable v = wf.declareJsonObj("v");
            wf.execute("noop", f ? v.jsonPath("$.field") : v);
        });
        pair("var-get-field", f -> wf -> {
            WfRunVariable v = wf.declareJsonObj("v");
            wf.execute("noop", f ? v.get("field") : v);
        });
        pair("var-assign", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(5);
        });

        // tasks
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
        pair("task-timeout", f -> wf -> {
            var node = wf.execute("noop");
            if (f) node.timeout(30);
        });
        pair("task-exponential-backoff", f -> wf -> {
            var node = wf.execute("noop");
            if (f) {
                node.withExponentialBackoff(ExponentialBackoffRetryPolicy.newBuilder()
                        .setBaseIntervalMs(1000)
                        .setMultiplier(2.0f)
                        .setMaxDelayMs(60000)
                        .build());
            }
        });
        pair("node-output-json-path", f -> wf -> {
            WfRunVariable v = wf.declareJsonObj("v");
            var node = wf.execute("noop");
            if (f) v.assign(node.jsonPath("$.total"));
        });
        pair("node-output-get", f -> wf -> {
            WfRunVariable v = wf.declareJsonObj("v");
            var node = wf.execute("noop");
            if (f) v.assign(node.get("total"));
        });

        // control flow
        // a condition only manifests through a conditional consumer, so the
        // nearest isolatable feature is the conditioned branch itself
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
        pair("wt-complete", f -> wf -> {
            wf.execute("noop");
            if (f) wf.complete("done");
        });
        pair("wt-retention-policy", f -> wf -> {
            wf.execute("noop");
            if (f) {
                wf.withRetentionPolicy(ThreadRetentionPolicy.newBuilder()
                        .setSecondsAfterThreadTermination(600)
                        .build());
            }
        });

        // sleep + waits
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

        // arithmetic + collection expressions
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
        pair("expr-pow", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(count.pow(2));
        });
        pair("expr-extend", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            wf.execute("noop");
            if (f) items.assign(items.extend("x"));
        });
        pair("expr-remove-index", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            wf.execute("noop");
            if (f) items.assign(items.removeIndex(0));
        });
        pair("expr-remove-if-present", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            wf.execute("noop");
            if (f) items.assign(items.removeIfPresent("x"));
        });
        pair("expr-remove-key", f -> wf -> {
            WfRunVariable obj = wf.declareJsonObj("obj");
            wf.execute("noop");
            if (f) obj.assign(obj.removeKey("k"));
        });
        pair("expr-size", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(items.size());
        });

        // WorkflowThread expression sugar (static-style helpers)
        pair("wt-add", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(wf.add(count, 1));
        });
        pair("wt-subtract", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(wf.subtract(count, 1));
        });
        pair("wt-multiply", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(wf.multiply(count, 2));
        });
        pair("wt-divide", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(wf.divide(count, 2));
        });
        pair("wt-pow", f -> wf -> {
            WfRunVariable count = wf.declareInt("count");
            wf.execute("noop");
            if (f) count.assign(wf.pow(count, 2));
        });
        pair("wt-extend", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            wf.execute("noop");
            if (f) items.assign(wf.extend(items, "x"));
        });
        pair("wt-remove-if-present", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            wf.execute("noop");
            if (f) items.assign(wf.removeIfPresent(items, "x"));
        });
        pair("wt-remove-index", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            wf.execute("noop");
            if (f) items.assign(wf.removeIndex(items, 0));
        });
        pair("wt-remove-key", f -> wf -> {
            WfRunVariable obj = wf.declareJsonObj("obj");
            wf.execute("noop");
            if (f) obj.assign(wf.removeKey(obj, "k"));
        });

        // comparison + logical expressions, consumed by waitForCondition
        pair("expr-is-equal-to", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isEqualTo(5));
        });
        pair("expr-is-not-equal-to", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isNotEqualTo(5));
        });
        pair("expr-is-greater-than", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isGreaterThan(5));
        });
        pair("expr-is-less-than", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isLessThan(5));
        });
        pair("var-is-greater-than-eq", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isGreaterThanEq(5));
        });
        pair("var-is-less-than-eq", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isLessThanEq(5));
        });
        pair("expr-is-in", f -> wf -> {
            WfRunVariable a = wf.declareStr("a");
            WfRunVariable allowed = wf.declareJsonArr("allowed");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isIn(allowed));
        });
        pair("expr-is-not-in", f -> wf -> {
            WfRunVariable a = wf.declareStr("a");
            WfRunVariable blocked = wf.declareJsonArr("blocked");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isNotIn(blocked));
        });
        pair("expr-does-contain", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            wf.execute("noop");
            if (f) wf.waitForCondition(items.doesContain("x"));
        });
        pair("expr-does-not-contain", f -> wf -> {
            WfRunVariable items = wf.declareJsonArr("items");
            wf.execute("noop");
            if (f) wf.waitForCondition(items.doesNotContain("x"));
        });
        pair("expr-and", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isGreaterThan(0).and(a.isLessThan(10)));
        });
        pair("expr-or", f -> wf -> {
            WfRunVariable a = wf.declareInt("a");
            wf.execute("noop");
            if (f) wf.waitForCondition(a.isLessThan(0).or(a.isGreaterThan(10)));
        });

        // casts
        pair("expr-cast-to", f -> wf -> {
            WfRunVariable v = wf.declareInt("v");
            WfRunVariable s = wf.declareStr("s");
            wf.execute("noop");
            if (f) s.assign(v.castTo(VariableType.STR));
        });
        pair("expr-cast-to-int", f -> wf -> {
            WfRunVariable s = wf.declareStr("s");
            WfRunVariable v = wf.declareInt("v");
            wf.execute("noop");
            if (f) v.assign(s.castToInt());
        });
        pair("expr-cast-to-double", f -> wf -> {
            WfRunVariable s = wf.declareStr("s");
            WfRunVariable v = wf.declareDouble("v");
            wf.execute("noop");
            if (f) v.assign(s.castToDouble());
        });
        pair("expr-cast-to-str", f -> wf -> {
            WfRunVariable n = wf.declareInt("n");
            WfRunVariable s = wf.declareStr("s");
            wf.execute("noop");
            if (f) s.assign(n.castToStr());
        });
        pair("expr-cast-to-bool", f -> wf -> {
            WfRunVariable s = wf.declareStr("s");
            WfRunVariable b = wf.declareBool("b");
            wf.execute("noop");
            if (f) b.assign(s.castToBool());
        });
        pair("expr-cast-to-bytes", f -> wf -> {
            WfRunVariable s = wf.declareStr("s");
            WfRunVariable b = wf.declareBytes("b");
            wf.execute("noop");
            if (f) b.assign(s.castToBytes());
        });
        pair("expr-cast-to-wf-run-id", f -> wf -> {
            WfRunVariable s = wf.declareStr("s");
            WfRunVariable w = wf.declareStr("w");
            wf.execute("noop");
            if (f) w.assign(s.castToWfRunId());
        });

        // mutations + formatting
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

        // structs
        pair("build-struct", f -> wf -> {
            WfRunVariable v = wf.declareStruct("v", "customer");
            wf.execute("noop");
            if (f) v.assign(wf.buildStruct("customer").put("name", "n"));
        });
        pair("build-inline-struct", f -> wf -> {
            WfRunVariable v = wf.declareStruct("v", "customer");
            wf.execute("noop");
            if (f) {
                v.assign(wf.buildStruct("customer")
                        .put("nested", wf.buildInlineStruct().put("k", "x")));
            }
        });

        // external events
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

        // child threads
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
        pair("wait-for-any-of", f -> wf -> {
            SpawnedThread child = wf.spawnThread(
                    c -> {
                        c.execute("child-task");
                    },
                    "child",
                    Map.of());
            if (f) wf.waitForAnyOf(SpawnedThreads.of(child));
        });
        pair("wait-for-first-of", f -> wf -> {
            SpawnedThread child = wf.spawnThread(
                    c -> {
                        c.execute("child-task");
                    },
                    "child",
                    Map.of());
            if (f) wf.waitForFirstOf(SpawnedThreads.of(child));
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
        pair("spawned-thread-number", f -> wf -> {
            WfRunVariable num = wf.declareInt("num");
            SpawnedThread child = wf.spawnThread(
                    c -> {
                        c.execute("child-task");
                    },
                    "child",
                    Map.of());
            if (f) num.assign(child.getThreadNumberVariable());
        });
        pair("wait-threads-handle-exception-on-child", f -> wf -> {
            SpawnedThread child = wf.spawnThread(
                    c -> {
                        c.execute("child-task");
                    },
                    "child",
                    Map.of());
            var wait = wf.waitForThreads(SpawnedThreads.of(child));
            if (f) {
                wait.handleExceptionOnChild("out-of-stock", handler -> {
                    handler.execute("reorder");
                });
            }
        });
        pair("wait-threads-handle-error-on-child", f -> wf -> {
            SpawnedThread child = wf.spawnThread(
                    c -> {
                        c.execute("child-task");
                    },
                    "child",
                    Map.of());
            var wait = wf.waitForThreads(SpawnedThreads.of(child));
            if (f) {
                wait.handleErrorOnChild(LHErrorType.CHILD_FAILURE, handler -> {
                    handler.execute("cleanup");
                });
            }
        });
        pair("wait-threads-handle-any-failure-on-child", f -> wf -> {
            SpawnedThread child = wf.spawnThread(
                    c -> {
                        c.execute("child-task");
                    },
                    "child",
                    Map.of());
            var wait = wf.waitForThreads(SpawnedThreads.of(child));
            if (f) {
                wait.handleAnyFailureOnChild(handler -> {
                    handler.execute("cleanup-any");
                });
            }
        });

        // child workflows
        pair("run-wf-inputs", f -> wf -> {
            WfRunVariable orderId = wf.declareStr("order-id");
            wf.runWf("child-wf", f ? Map.of("order-id", orderId) : Map.of());
        });
        pair("wait-for-child-wf", f -> wf -> {
            SpawnedChildWf child = wf.runWf("child-wf", Map.of());
            if (f) wf.waitForChildWf(child);
        });

        // interrupts
        pair("interrupt-handler", f -> wf -> {
            wf.execute("main-step");
            if (f) {
                wf.registerInterruptHandler("cancel-requested", handler -> {
                    handler.execute("cancel");
                });
            }
        });

        // failure handling
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

        // user tasks
        pair("assign-user-task", f -> wf -> {
            if (f) wf.assignUserTask("approve-request", "alice", "approvers");
        });
        pair("user-task-notes", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) ut.withNotes("Please review");
        });
        pair("user-task-on-cancellation-exception", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) ut.withOnCancellationException("cancelled");
        });
        pair("release-to-group-on-deadline", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) wf.releaseToGroupOnDeadline(ut, 300);
        });
        pair("reassign-user-task", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) wf.reassignUserTask(ut, "bob", "approvers", 300);
        });
        pair("schedule-reminder-task", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) wf.scheduleReminderTask(ut, 60, "send-reminder");
        });
        pair("schedule-reminder-on-assignment", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) wf.scheduleReminderTaskOnAssignment(ut, 60, "send-reminder");
        });
        pair("cancel-user-task-run-after", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) wf.cancelUserTaskRunAfter(ut, 86400);
        });
        pair("cancel-user-task-after-assignment", f -> wf -> {
            UserTaskOutput ut = wf.assignUserTask("approve-request", "alice", "approvers");
            if (f) wf.cancelUserTaskRunAfterAssignment(ut, 86400);
        });

        // Workflow-level configuration
        pairWf("wf-update-type", f -> {
            Workflow w = Workflow.newWorkflow("probe-wf-update-type", wf -> {
                wf.execute("noop");
            });
            if (f) w.withUpdateType(AllowedUpdateType.NO_UPDATES);
            return w;
        });
        pairWf("wf-set-parent", f -> {
            Workflow w = Workflow.newWorkflow("probe-wf-set-parent", wf -> {
                wf.execute("noop");
            });
            if (f) w.setParent("parent-wf");
            return w;
        });
        pairWf("wf-retention-policy", f -> {
            Workflow w = Workflow.newWorkflow("probe-wf-retention-policy", wf -> {
                wf.execute("noop");
            });
            if (f) {
                w.withRetentionPolicy(WorkflowRetentionPolicy.newBuilder()
                        .setSecondsAfterWfTermination(3600)
                        .build());
            }
            return w;
        });
        pairWf("wf-default-thread-retention", f -> {
            Workflow w = Workflow.newWorkflow("probe-wf-default-thread-retention", wf -> {
                wf.execute("noop");
            });
            if (f) {
                w.withDefaultThreadRetentionPolicy(ThreadRetentionPolicy.newBuilder()
                        .setSecondsAfterThreadTermination(600)
                        .build());
            }
            return w;
        });
        pairWf("wf-default-task-timeout", f -> {
            Workflow w = Workflow.newWorkflow("probe-wf-default-task-timeout", wf -> {
                wf.execute("noop");
            });
            if (f) w.setDefaultTaskTimeout(45);
            return w;
        });
        pairWf("wf-default-task-retries", f -> {
            Workflow w = Workflow.newWorkflow("probe-wf-default-task-retries", wf -> {
                wf.execute("noop");
            });
            if (f) w.setDefaultTaskRetries(2);
            return w;
        });
        pairWf("wf-default-task-backoff", f -> {
            Workflow w = Workflow.newWorkflow("probe-wf-default-task-backoff", wf -> {
                wf.execute("noop");
            });
            if (f) {
                w.setDefaultTaskExponentialBackoffPolicy(ExponentialBackoffRetryPolicy.newBuilder()
                        .setBaseIntervalMs(1000)
                        .setMultiplier(2.0f)
                        .setMaxDelayMs(60000)
                        .build());
            }
            return w;
        });
    }

    static Set<String> caseIds() {
        Set<String> ids = new LinkedHashSet<>(SINGLES.keySet());
        ids.addAll(PAIRS.keySet());
        return ids;
    }

    static String compile(String caseId, String variant) {
        if (SINGLES.containsKey(caseId)) {
            if (!variant.equals("feature")) {
                throw new IllegalArgumentException(
                        "case " + caseId + " is single-variant; only --variant feature exists");
            }
            return SINGLES.get(caseId).compileWfToJson();
        }
        Function<Boolean, Workflow> builder = PAIRS.get(caseId);
        if (builder == null) {
            throw new IllegalArgumentException("unknown case: " + caseId);
        }
        boolean withFeature =
                switch (variant) {
                    case "base" -> false;
                    case "feature" -> true;
                    default -> throw new IllegalArgumentException("variant must be base|feature: " + variant);
                };
        return builder.apply(withFeature).compileWfToJson();
    }

    static Map<String, Function<Boolean, Workflow>> pairs() {
        return PAIRS;
    }

    static Map<String, Workflow> singles() {
        return SINGLES;
    }
}
