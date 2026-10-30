package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.adapter.LHIntegerAdapter;
import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.FailureHandlerDef.FailureToCatchCase;
import io.littlehorse.sdk.common.proto.FailureHandlerDef.LHFailureType;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.SleepNode.SleepLengthCase;
import io.littlehorse.sdk.common.proto.TaskNode;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTATask;
import io.littlehorse.sdk.common.proto.UserTaskNode;
import io.littlehorse.sdk.common.proto.VariableAssignment.FormatString;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode.ThreadsToWaitForCase;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
import io.littlehorse.sdk.wfsdk.LHFormatString;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkflowThreadImplTest {

    Faker faker = new Faker();

    @Test
    void testSleepUntil() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    WfRunVariable var = thread.addVariable("my-var", VariableType.INT);
                    thread.sleepUntil(var);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();

        Node sleepNode = wfSpec.getThreadSpecsOrThrow("entrypoint").getNodesOrThrow("1-sleep-SLEEP");

        assertThat(sleepNode.getSleep()).isNotNull();
        assertEquals(sleepNode.getSleep().getSleepLengthCase(), SleepLengthCase.TIMESTAMP);
        assertEquals(sleepNode.getSleep().getTimestamp().getVariableName(), "my-var");
    }

    @Test
    void testEarlyReturn() {
        Workflow workflow = Workflow.newWorkflow(
                "asdf",
                wf -> {
                    wf.execute("some-task");
                    wf.fail("some-exception", "some error message");
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = workflow.compileWorkflow();

        List<Map.Entry<String, Node>> exitNodes =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesMap().entrySet().stream()
                        .filter(nodePair -> {
                            return (nodePair.getValue().getNodeCase() == NodeCase.EXIT);
                        })
                        .collect(Collectors.toList());

        assertEquals(exitNodes.size(), 1);
    }

    @Test
    void testEarlyReturnInIfStatement() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    WfRunVariable var = thread.addVariable("my-var", VariableType.INT);
                    thread.doIf(thread.condition(var, Comparator.LESS_THAN, 10), ifBody -> {
                        ifBody.execute("foo");
                        ifBody.complete();
                    });
                    thread.execute("bar");
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        List<Map.Entry<String, Node>> exitNodes =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesMap().entrySet().stream()
                        .filter(nodePair -> {
                            return (nodePair.getValue().getNodeCase() == NodeCase.EXIT);
                        })
                        .collect(Collectors.toList());

        assertEquals(exitNodes.size(), 2);

        exitNodes.forEach(entry -> {
            Node node = entry.getValue();
            assertThat(node.getOutgoingEdgesCount()).isEqualTo(0);
        });
    }

    @AllArgsConstructor
    class Foo {

        public String bar;
        public String baz;
    }

    @Test
    void testDefaultVarVals() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.addVariable("int-var", 123);
                    thread.addVariable("object-var", new Foo("asdf", "fdsa"));
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        List<ThreadVarDef> varDefs =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getVariableDefsList();

        VariableDef intVar = varDefs.get(0).getVarDef();
        assertThat(intVar.getDefaultValue()).isNotNull();
        assertEquals(intVar.getDefaultValue().getInt(), 123);

        VariableDef objVar = varDefs.get(1).getVarDef();
        assertThat(objVar.getDefaultValue()).isNotEqualTo(null);
        assertEquals(objVar.getTypeDef().getPrimitiveType(), VariableType.JSON_OBJ);
    }

    @Test
    void testWhileLoopConditional() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.execute("asdf");
                    thread.doWhile(thread.condition("asf", Comparator.EQUALS, "asf"), loop -> {
                        loop.execute("fdsa");
                    });
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        Node lastNodeInLoopBody =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("2-nop-NOP");

        assertThat(lastNodeInLoopBody.getOutgoingEdgesCount()).isEqualTo(2);
    }

    @Test
    void noRetriesByDefault() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.execute("asdf");
                },
                new LHConfig());
        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        Node taskNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("1-asdf-TASK");

        assertThat(taskNode.getTask().getRetries()).isEqualTo(0);
    }

    @Test
    void setIndividualRetry() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.execute("asdf").withRetries(2);
                    ;
                },
                new LHConfig());
        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        Node taskNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("1-asdf-TASK");

        assertThat(taskNode.getTask().getRetries()).isEqualTo(2);
    }

    @Test
    void setDefaultRetryAndOverride() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.execute("asdf");
                    thread.execute("asdf").withRetries(2);
                },
                new LHConfig());
        wf.setDefaultTaskRetries(1);
        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        Node defaultNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("1-asdf-TASK");

        assertThat(defaultNode.getTask().getRetries()).isEqualTo(1);

        Node overridenNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("2-asdf-TASK");
        assertThat(overridenNode.getTask().getRetries()).isEqualTo(2);
    }

    @Test
    void setDefaultExponentialBackoffPolicyAndOverride() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.execute("asdf");
                    thread.execute("asdf").withRetries(2);
                    thread.execute("asdf")
                            .withRetries(42)
                            .withExponentialBackoff(ExponentialBackoffRetryPolicy.newBuilder()
                                    .setBaseIntervalMs(500)
                                    .setMultiplier(137)
                                    .setMaxDelayMs(100000)
                                    .build());
                },
                new LHConfig());
        wf.setDefaultTaskExponentialBackoffPolicy(ExponentialBackoffRetryPolicy.newBuilder()
                .setBaseIntervalMs(500)
                .setMultiplier(2)
                .setMaxDelayMs(50000)
                .build());
        wf.setDefaultTaskRetries(5);
        PutWfSpecRequest wfSpec = wf.compileWorkflow();

        Node defaultNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("1-asdf-TASK");
        assertThat(defaultNode.getTask().getExponentialBackoff().getBaseIntervalMs())
                .isEqualTo(500);
        assertThat(defaultNode.getTask().getRetries()).isEqualTo(5);

        Node overridenNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("2-asdf-TASK");
        assertThat(overridenNode.getTask().getRetries()).isEqualTo(2);

        Node overridenNodeWithExponential =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("3-asdf-TASK");
        assertThat(overridenNodeWithExponential
                        .getTask()
                        .getExponentialBackoff()
                        .getMultiplier())
                .isEqualTo(137);
    }

    @Test
    void shouldCaptureWorkflowEventDefNames() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.throwEvent("event-one", "fdsa");
                    thread.spawnThread(
                            child -> {
                                child.throwEvent("event-from-child", "asdf");
                            },
                            "child",
                            null);
                },
                new LHConfig());

        Assertions.assertThat(wf.getRequiredWorkflowEventDefNames()).contains("event-one");
        Assertions.assertThat(wf.getRequiredWorkflowEventDefNames()).contains("event-from-child");
    }

    @Test
    void shouldUseTypeAdapterForWorkflowEventRegisteredAsClass() {
        LHConfig config =
                LHConfig.newBuilder().addTypeAdapter(new UUIDConfigAdapter()).build();
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.throwEvent("event-one", UUID.randomUUID()).registeredAs(UUID.class);
                },
                config);

        wf.compileWorkflow();

        PutWorkflowEventDefRequest eventDef =
                wf.getWorkflowEventDefsToRegister().stream().findFirst().orElseThrow();
        assertThat(eventDef.getContentType().getReturnType().getPrimitiveType()).isEqualTo(VariableType.STR);
    }

    @Test
    void shouldUseTypeAdapterBeforeAnyWorkflowCompilation() {
        UUID defaultValue = UUID.randomUUID();
        LHConfig config =
                LHConfig.newBuilder().addTypeAdapter(new UUIDIntegerAdapter()).build();
        Workflow wf = Workflow.newWorkflow(
                "configured-workflow",
                thread -> {
                    thread.addVariable("id", defaultValue);
                },
                config);

        assertThat(wf.getRequiredTaskDefNames()).isEmpty();
        VariableDef id = wf.compileWorkflow()
                .getThreadSpecsOrThrow("entrypoint")
                .getVariableDefs(0)
                .getVarDef();

        assertThat(id.getTypeDef().getPrimitiveType()).isEqualTo(VariableType.INT);
        assertThat(id.getDefaultValue().getInt()).isEqualTo(defaultValue.hashCode());
    }

    @Test
    void shouldUseTypeAdapterForExternalEventRegisteredAsClass() {
        LHConfig config =
                LHConfig.newBuilder().addTypeAdapter(new UUIDConfigAdapter()).build();
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.waitForEvent("event-one").registeredAs(UUID.class);
                },
                config);

        wf.compileWorkflow();

        PutExternalEventDefRequest eventDef =
                wf.getExternalEventDefsToRegister().stream().findFirst().orElseThrow();
        assertThat(eventDef.getContentType().getReturnType().getPrimitiveType()).isEqualTo(VariableType.STR);
    }

    @Test
    void shouldUseTypeAdapterFromConfigForWorkflowEventRegisteredAsClass() {
        LHConfig config =
                LHConfig.newBuilder().addTypeAdapter(new UUIDConfigAdapter()).build();
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    WfRunVariable strVar = thread.declareStr("some-var");
                    thread.throwEvent("event-one", strVar).registeredAs(UUID.class);
                },
                config);

        wf.compileWorkflow();

        PutWorkflowEventDefRequest eventDef =
                wf.getWorkflowEventDefsToRegister().stream().findFirst().orElseThrow();
        assertThat(eventDef.getContentType().getReturnType().getPrimitiveType()).isEqualTo(VariableType.STR);
    }

    @Test
    void setDefaultTaskTimeout() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
                    thread.execute("asdf");
                    thread.execute("asdf").timeout(42);
                },
                new LHConfig());
        wf.setDefaultTaskTimeout(19);
        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        Node taskNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("1-asdf-TASK");

        assertThat(taskNode.getTask().getTimeoutSeconds()).isEqualTo(19);

        Node overridenNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("2-asdf-TASK");
        assertThat(overridenNode.getTask().getTimeoutSeconds()).isEqualTo(42);
    }

    @Test
    void ensureNoWfSpecGcByDefault() {
        Workflow wf = Workflow.newWorkflow(
                "some-wf",
                thread -> {
                    thread.execute("some-task");
                },
                new LHConfig());
        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        assertThat(wfSpec.hasRetentionPolicy()).isFalse();

        assertThat(wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName())
                        .hasRetentionPolicy())
                .isFalse();
    }

    @Test
    void testWfRetentionPolicy() {
        Workflow wf = Workflow.newWorkflow(
                        "some-wf",
                        thread -> {
                            thread.execute("some-task");
                        },
                        new LHConfig())
                .withRetentionPolicy(WorkflowRetentionPolicy.newBuilder()
                        .setSecondsAfterWfTermination(Duration.ofDays(30).toSeconds())
                        .build());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();

        assertThat(wfSpec.hasRetentionPolicy()).isTrue();

        assertThat(wfSpec.getRetentionPolicy().getSecondsAfterWfTermination() == (24 * 30 * 3600));

        assertThat(wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName())
                        .hasRetentionPolicy())
                .isFalse();
    }

    @Test
    void testDefaultThreadRetentionPolicy() {
        Workflow wf = Workflow.newWorkflow(
                        "some-wf",
                        thread -> {
                            thread.execute("some-task");
                        },
                        new LHConfig())
                .withDefaultThreadRetentionPolicy(ThreadRetentionPolicy.newBuilder()
                        .setSecondsAfterThreadTermination(30)
                        .build());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        assertThat(wfSpec.hasRetentionPolicy()).isFalse();

        ThreadSpec thread = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        assertThat(thread.hasRetentionPolicy()).isTrue();
        assertThat(thread.getRetentionPolicy().getSecondsAfterThreadTermination())
                .isEqualTo(30);
    }

    @Test
    void testOverrideThreadRetentionPolicy() {
        Workflow wf = Workflow.newWorkflow(
                        "some-wf",
                        thread -> {
                            thread.execute("some-task");
                            thread.spawnThread(
                                    child -> {
                                        child.withRetentionPolicy(ThreadRetentionPolicy.newBuilder()
                                                .setSecondsAfterThreadTermination(50)
                                                .build());
                                        child.execute("another-task");
                                    },
                                    "child-thread",
                                    null);
                        },
                        new LHConfig())
                .withDefaultThreadRetentionPolicy(ThreadRetentionPolicy.newBuilder()
                        .setSecondsAfterThreadTermination(30)
                        .build());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        assertThat(wfSpec.hasRetentionPolicy()).isFalse();

        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        assertThat(entrypoint.hasRetentionPolicy()).isTrue();
        assertThat(entrypoint.getRetentionPolicy().getSecondsAfterThreadTermination())
                .isEqualTo(30);

        ThreadSpec child = wfSpec.getThreadSpecsOrThrow("child-thread");
        assertThat(child.getRetentionPolicy().getSecondsAfterThreadTermination())
                .isEqualTo(50);
    }

    @Test
    void testTimeoutOnExternalEventNode() {
        Workflow wf = Workflow.newWorkflow(
                "some-wf",
                thread -> {
                    thread.waitForEvent("some-event").timeout(10);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("1-some-event-EXTERNAL_EVENT");
        assertThat(node.getExternalEvent().getTimeoutSeconds().getLiteralValue().getInt())
                .isEqualTo(10);
    }

    @Test
    void shouldAutomaticallyMaskCorrelationIdFromMaskedVar() {
        Workflow wfGen = Workflow.newWorkflow(
                "some-wf",
                wf -> {
                    WfRunVariable ssn = wf.declareStr("ssn").masked();
                    wf.waitForEvent("identity-verified").withCorrelationId(ssn);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wfGen.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("1-identity-verified-EXTERNAL_EVENT");
        assertThat(node.getExternalEvent().getMaskCorrelationKey()).isTrue();
    }

    @Test
    void shouldNotMaskNormalVariableAsCorrelationId() {
        Workflow wfGen = Workflow.newWorkflow(
                "some-wf",
                wf -> {
                    WfRunVariable email = wf.declareStr("email");
                    wf.waitForEvent("identity-verified").withCorrelationId(email);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wfGen.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("1-identity-verified-EXTERNAL_EVENT");
        assertThat(node.getExternalEvent().getMaskCorrelationKey()).isFalse();
    }

    @Test
    void shouldMaskCorrelationIdIfITellItTo() {
        Workflow wfGen = Workflow.newWorkflow(
                "some-wf",
                wf -> {
                    WfRunVariable email = wf.declareStr("email");
                    wf.waitForEvent("identity-verified").withCorrelationId(email, true);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wfGen.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("1-identity-verified-EXTERNAL_EVENT");
        assertThat(node.getExternalEvent().getMaskCorrelationKey()).isTrue();
    }

    @Test
    void shouldNotMaskCorrelationIdIfITellItNotTo() {
        Workflow wfGen = Workflow.newWorkflow(
                "some-wf",
                wf -> {
                    WfRunVariable ssn = wf.declareStr("ssn").masked();
                    wf.waitForEvent("identity-verified").withCorrelationId(ssn, false);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wfGen.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("1-identity-verified-EXTERNAL_EVENT");
        assertThat(node.getExternalEvent().getMaskCorrelationKey()).isFalse();
    }

    @Test
    void testWaitForThreadsHandleAnyFailureOnChild() {
        Workflow workflow = Workflow.newWorkflow(
                "some-wf",
                wf -> {
                    SpawnedThread child = wf.spawnThread(childThread -> {}, "child", Map.of());
                    WaitForThreadsNodeOutput result = wf.waitForThreads(SpawnedThreads.of(child));
                    result.handleAnyFailureOnChild(handler -> {});
                },
                new LHConfig());
        PutWfSpecRequest wfSpec = workflow.compileWorkflow();

        assertThat(wfSpec.getThreadSpecsCount()).isEqualTo(3);
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("2-threads-WAIT_FOR_THREADS");
        WaitForThreadsNode wftn = node.getWaitForThreads();

        assertThat(wftn.getPerThreadFailureHandlersCount()).isEqualTo(1);
        assertThat(wftn.getPerThreadFailureHandlers(0).getFailureToCatchCase())
                .isEqualTo(FailureToCatchCase.FAILURETOCATCH_NOT_SET);
        assertThat(wftn.getPerThreadFailureHandlers(0).getHandlerSpecName())
                .isEqualTo("failure-handler-2-threads-WAIT_FOR_THREADS-ANY_FAILURE");
    }

    @Test
    void testWaitForThreadsHandleAnyErrorOnChild() {
        Workflow workflow = Workflow.newWorkflow(
                "some-wf",
                wf -> {
                    SpawnedThread child = wf.spawnThread(childThread -> {}, "child", Map.of());
                    WaitForThreadsNodeOutput result = wf.waitForThreads(SpawnedThreads.of(child));
                    result.handleErrorOnChild(LHErrorType.TIMEOUT, handler -> {});
                    result.handleErrorOnChild(null, handler -> {});
                },
                new LHConfig());
        PutWfSpecRequest wfSpec = workflow.compileWorkflow();

        assertThat(wfSpec.getThreadSpecsCount()).isEqualTo(4);
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("2-threads-WAIT_FOR_THREADS");
        WaitForThreadsNode wftn = node.getWaitForThreads();

        assertThat(wftn.getPerThreadFailureHandlersCount()).isEqualTo(2);
        FailureHandlerDef timeoutHandler = wftn.getPerThreadFailureHandlers(0);
        FailureHandlerDef anyErrorHandler = wftn.getPerThreadFailureHandlers(1);

        assertThat(timeoutHandler.getFailureToCatchCase()).isEqualTo(FailureToCatchCase.SPECIFIC_FAILURE);
        assertThat(timeoutHandler.getSpecificFailure()).isEqualTo(LHErrorType.TIMEOUT.toString());
        assertThat(timeoutHandler.getHandlerSpecName()).isEqualTo("error-handler-2-threads-WAIT_FOR_THREADS-TIMEOUT");

        assertThat(anyErrorHandler.getFailureToCatchCase()).isEqualTo(FailureToCatchCase.ANY_FAILURE_OF_TYPE);
        assertThat(anyErrorHandler.getAnyFailureOfType()).isEqualTo(LHFailureType.FAILURE_TYPE_ERROR);
        assertThat(anyErrorHandler.getHandlerSpecName())
                .isEqualTo("error-handler-2-threads-WAIT_FOR_THREADS-FAILURE_TYPE_ERROR");
    }

    @Test
    void testWaitForThreadsHandleAnyExceptionOnChild() {
        Workflow workflow = Workflow.newWorkflow(
                "some-wf",
                wf -> {
                    SpawnedThread child = wf.spawnThread(childThread -> {}, "child", Map.of());
                    WaitForThreadsNodeOutput result = wf.waitForThreads(SpawnedThreads.of(child));
                    result.handleExceptionOnChild("my-exception", handler -> {});
                    result.handleExceptionOnChild(null, handler -> {});
                },
                new LHConfig());
        PutWfSpecRequest wfSpec = workflow.compileWorkflow();

        assertThat(wfSpec.getThreadSpecsCount()).isEqualTo(4);
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("2-threads-WAIT_FOR_THREADS");
        WaitForThreadsNode wftn = node.getWaitForThreads();

        assertThat(wftn.getPerThreadFailureHandlersCount()).isEqualTo(2);
        FailureHandlerDef specificHandler = wftn.getPerThreadFailureHandlers(0);
        FailureHandlerDef anyHandler = wftn.getPerThreadFailureHandlers(1);

        assertThat(specificHandler.getFailureToCatchCase()).isEqualTo(FailureToCatchCase.SPECIFIC_FAILURE);
        assertThat(specificHandler.getSpecificFailure()).isEqualTo("my-exception");
        assertThat(specificHandler.getHandlerSpecName())
                .isEqualTo("exn-handler-2-threads-WAIT_FOR_THREADS-my-exception");

        assertThat(anyHandler.getFailureToCatchCase()).isEqualTo(FailureToCatchCase.ANY_FAILURE_OF_TYPE);
        assertThat(anyHandler.getAnyFailureOfType()).isEqualTo(LHFailureType.FAILURE_TYPE_EXCEPTION);
        assertThat(anyHandler.getHandlerSpecName())
                .isEqualTo("exn-handler-2-threads-WAIT_FOR_THREADS-FAILURE_TYPE_EXCEPTION");
    }

    @Test
    void testWaitForParallelSpawnThreads() {
        Workflow workflow = Workflow.newWorkflow(
                "some-wf",
                wf -> {
                    WfRunVariable toSpawn = wf.addVariable("to-spawn", VariableType.JSON_ARR);
                    wf.waitForThreads(wf.spawnThreadForEach(toSpawn, "child", child -> {}));
                },
                new LHConfig());
        PutWfSpecRequest wfSpec = workflow.compileWorkflow();

        assertThat(wfSpec.getThreadSpecsCount()).isEqualTo(2);
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("2-threads-WAIT_FOR_THREADS");
        WaitForThreadsNode wftn = node.getWaitForThreads();

        assertThat(wftn.getThreadsToWaitForCase()).isEqualTo(ThreadsToWaitForCase.THREAD_LIST);
        assertThat(wftn.getThreadList().getVariableName()).isEqualTo("1-child-START_MULTIPLE_THREADS");
    }

    @Test
    void testThrowEventNode() {
        Workflow workflow = Workflow.newWorkflow(
                "throw-event-wf",
                wf -> {
                    WfRunVariable var = wf.addVariable("my-var", VariableType.STR);
                    wf.throwEvent("my-event", var);
                    wf.throwEvent("another-event", "some-content");
                },
                new LHConfig());
        PutWfSpecRequest wfSpec = workflow.compileWorkflow();

        assertThat(wfSpec.getThreadSpecsCount()).isEqualTo(1);
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        assertThat(entrypoint.getNodesCount()).isEqualTo(4);

        Node firstThrow = entrypoint.getNodesOrThrow("1-throw-my-event-THROW_EVENT");
        assertThat(firstThrow.getThrowEvent().getEventDefId().getName()).isEqualTo("my-event");
        assertThat(firstThrow.getThrowEvent().getContent().getVariableName()).isEqualTo("my-var");

        Node secondThrow = entrypoint.getNodesOrThrow("2-throw-another-event-THROW_EVENT");
        assertThat(secondThrow.getThrowEvent().getEventDefId().getName()).isEqualTo("another-event");
        assertThat(secondThrow.getThrowEvent().getContent().getLiteralValue().getStr())
                .isEqualTo("some-content");
    }

    @Test
    void testReminderTaskWithNoArguments() {
        Workflow workflow = Workflow.newWorkflow(
                "throw-event-wf",
                wf -> {
                    UserTaskOutput uto = wf.assignUserTask("some-usertaskdef", "some-person", "some-group");
                    wf.scheduleReminderTask(uto, 10, "some-taskdef");
                },
                new LHConfig());
        PutWfSpecRequest wfSpec = workflow.compileWorkflow();

        assertThat(wfSpec.getThreadSpecsCount()).isEqualTo(1);
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());

        Node node = entrypoint.getNodesOrThrow("1-some-usertaskdef-USER_TASK");
        assertThat(node.getNodeCase()).isEqualTo(NodeCase.USER_TASK);

        UserTaskNode utn = node.getUserTask();
        assertThat(utn.getActionsCount()).isEqualTo(1);

        UTATask taskTrigger = utn.getActions(0).getTask();
        assertThat(taskTrigger.getTask().getVariablesCount()).isEqualTo(0);
    }

    @Test
    void testDynamicTask() {
        Workflow workflow = Workflow.newWorkflow(
                "obiwan",
                wf -> {
                    WfRunVariable myVar = wf.addVariable("my-var", VariableType.STR);
                    wf.execute("some-static-task");

                    LHFormatString formatStr = wf.format("some-dynamic-task-{0}", myVar);
                    wf.execute(formatStr);
                    wf.execute(myVar);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = workflow.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());

        Node staticNode = entrypoint.getNodesOrThrow("1-some-static-task-TASK");
        assertEquals("some-static-task", staticNode.getTask().getTaskDefId().getName());

        Node formatStrNode = entrypoint.getNodesOrThrow("2-some-dynamic-task-{0}-TASK");
        FormatString formatStr = formatStrNode.getTask().getDynamicTask().getFormatString();
        assertEquals(
                "some-dynamic-task-{0}", formatStr.getFormat().getLiteralValue().getStr());

        Node varNode = entrypoint.getNodesOrThrow("3-my-var-TASK");
        assertEquals("my-var", varNode.getTask().getDynamicTask().getVariableName());
    }

    @Test
    void testPassingNodeOutput() {
        Workflow workflow = Workflow.newWorkflow(
                "obiwan",
                wf -> {
                    wf.execute("some-task", wf.execute("some-other-task"));
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = workflow.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());

        Node node = entrypoint.getNodesOrThrow("2-some-task-TASK");
        TaskNode taskNode = node.getTask();

        assertThat(taskNode.getVariables(0).getNodeOutput().getNodeName()).isEqualTo("1-some-other-task-TASK");
    }

    @Test
    void mutationsShouldUseVariableAssignment() {
        // Deprecated the literal_value and node_output approach
        Workflow workflow = Workflow.newWorkflow(
                "obiwan",
                wf -> {
                    WfRunVariable myVar = wf.addVariable("my-var", VariableType.STR);
                    myVar.assign("some-value");
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = workflow.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("0-entrypoint-ENTRYPOINT");

        Edge edge = node.getOutgoingEdges(0);
        assertThat(edge.getVariableMutations(0)
                        .getRhsAssignment()
                        .getLiteralValue()
                        .getStr())
                .isEqualTo("some-value");
    }

    @Test
    void nodeOutputMutationsShouldAlsoUseVariableAssignments() {
        // Deprecated the literal_value and node_output approach
        Workflow workflow = Workflow.newWorkflow(
                "obiwan",
                wf -> {
                    WfRunVariable myVar = wf.addVariable("my-var", VariableType.STR);
                    myVar.assign(wf.execute("use-the-force"));
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = workflow.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("1-use-the-force-TASK");

        Edge edge = node.getOutgoingEdges(0);
        assertThat(edge.getVariableMutations(0)
                        .getRhsAssignment()
                        .getNodeOutput()
                        .getNodeName())
                .isEqualTo("1-use-the-force-TASK");
    }

    @Test
    void nodeOutputMutationsShouldCarryJsonPath() {
        // Deprecated the literal_value and node_output approach
        Workflow workflow = Workflow.newWorkflow(
                "obiwan",
                wf -> {
                    WfRunVariable myVar = wf.addVariable("my-var", VariableType.STR);
                    myVar.assign(wf.execute("use-the-force").jsonPath("$.hello.there"));
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = workflow.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("1-use-the-force-TASK");

        Edge edge = node.getOutgoingEdges(0);
        assertThat(edge.getVariableMutations(0)
                        .getRhsAssignment()
                        .getNodeOutput()
                        .getNodeName())
                .isEqualTo("1-use-the-force-TASK");

        assertThat(edge.getVariableMutations(0).getRhsAssignment().getJsonPath())
                .isEqualTo("$.hello.there");
    }

    @Test
    void assigningVariablesToOtherVariablesShouldUseVariableAssignment() {
        // Deprecated the literal_value and node_output approach
        Workflow workflow = Workflow.newWorkflow(
                "obiwan",
                wf -> {
                    WfRunVariable myVar = wf.addVariable("my-var", VariableType.STR);
                    WfRunVariable otherVar = wf.addVariable("other-var", VariableType.STR);
                    myVar.assign(otherVar);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = workflow.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("0-entrypoint-ENTRYPOINT");

        Edge edge = node.getOutgoingEdges(0);
        assertThat(edge.getVariableMutations(0).getRhsAssignment().getVariableName())
                .isEqualTo("other-var");
    }

    @Test
    void assigningVariablesToOtherVariablesShouldCarryJsonPath() {
        // Deprecated the literal_value and node_output approach
        Workflow workflow = Workflow.newWorkflow(
                "obiwan",
                wf -> {
                    WfRunVariable myVar = wf.addVariable("my-var", VariableType.STR);
                    WfRunVariable otherVar = wf.addVariable("other-var", VariableType.JSON_OBJ);
                    myVar.assign(otherVar.jsonPath("$.hello.there"));
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = workflow.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("0-entrypoint-ENTRYPOINT");

        Edge edge = node.getOutgoingEdges(0);
        assertThat(edge.getVariableMutations(0).getRhsAssignment().getVariableName())
                .isEqualTo("other-var");

        assertThat(edge.getVariableMutations(0).getRhsAssignment().getJsonPath())
                .isEqualTo("$.hello.there");
    }

    @Test
    public void ShouldThrowANullPointerExceptionWhenParentWorkflowIsNull() {
        NullPointerException e =
                assertThrows(NullPointerException.class, () -> new WorkflowThreadImpl("my-thread", null, null));

        assertEquals("Parent workflow can not be null.", e.getMessage());
    }

    @Test()
    public void shouldValidateEmptyUserNameOnUserTaskDefinition() {
        Throwable throwable = Assertions.catchThrowable(() -> {
            Workflow.newWorkflow(
                            "my-wf",
                            thread -> {
                                thread.assignUserTask("some-usertaskdef", "", null);
                            },
                            new LHConfig())
                    .compileWorkflow();
        });
        Assertions.assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThat(throwable.getMessage()).isEqualTo("UserId can't be empty");
    }

    @Test()
    public void shouldValidateEmptyGroupNameOnUserTaskDefinition() {
        Throwable throwable = Assertions.catchThrowable(() -> {
            Workflow.newWorkflow(
                            "my-wf",
                            thread -> {
                                thread.assignUserTask("some-usertaskdef", null, "");
                            },
                            new LHConfig())
                    .compileWorkflow();
        });
        Assertions.assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThat(throwable.getMessage()).isEqualTo("UserGroup can't be empty");
    }

    @Test
    void testCompleteWithoutReturnValue() {
        Workflow wf = Workflow.newWorkflow(
                "test-workflow",
                thread -> {
                    thread.execute("some-task");
                    thread.complete();
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();

        // Find the exit node
        List<Map.Entry<String, Node>> exitNodes =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesMap().entrySet().stream()
                        .filter(nodePair -> nodePair.getValue().getNodeCase() == NodeCase.EXIT)
                        .collect(Collectors.toList());

        assertEquals(exitNodes.size(), 1);
        Node exitNode = exitNodes.get(0).getValue();
        assertThat(exitNode.getExit().hasReturnContent()).isFalse();
    }

    @Test
    void testCompleteWithLiteralReturnValue() {
        Workflow wf = Workflow.newWorkflow(
                "test-workflow",
                thread -> {
                    thread.execute("some-task");
                    thread.complete("success");
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();

        // Find the exit node
        List<Map.Entry<String, Node>> exitNodes =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesMap().entrySet().stream()
                        .filter(nodePair -> nodePair.getValue().getNodeCase() == NodeCase.EXIT)
                        .collect(Collectors.toList());

        assertEquals(exitNodes.size(), 1);
        Node exitNode = exitNodes.get(0).getValue();
        assertThat(exitNode.getExit().hasReturnContent()).isTrue();
        assertThat(exitNode.getExit().getReturnContent().hasLiteralValue()).isTrue();
        assertEquals(exitNode.getExit().getReturnContent().getLiteralValue().getStr(), "success");
    }

    @Test
    void testCompleteWithVariableReturnValue() {
        Workflow wf = Workflow.newWorkflow(
                "test-workflow",
                thread -> {
                    WfRunVariable result = thread.addVariable("result", VariableType.STR);
                    thread.execute("some-task");
                    thread.complete(result);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();

        // Find the exit node
        List<Map.Entry<String, Node>> exitNodes =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesMap().entrySet().stream()
                        .filter(nodePair -> nodePair.getValue().getNodeCase() == NodeCase.EXIT)
                        .collect(Collectors.toList());

        assertEquals(exitNodes.size(), 1);
        Node exitNode = exitNodes.get(0).getValue();
        assertThat(exitNode.getExit().hasReturnContent()).isTrue();
        assertThat(exitNode.getExit().getReturnContent().hasVariableName()).isTrue();
        assertEquals(exitNode.getExit().getReturnContent().getVariableName(), "result");
    }

    @Test
    void testCompleteWithNodeOutputReturnValue() {
        Workflow wf = Workflow.newWorkflow(
                "test-workflow",
                thread -> {
                    var taskOutput = thread.execute("some-task");
                    thread.complete(taskOutput);
                },
                new LHConfig());

        PutWfSpecRequest wfSpec = wf.compileWorkflow();

        // Find the exit node
        List<Map.Entry<String, Node>> exitNodes =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesMap().entrySet().stream()
                        .filter(nodePair -> nodePair.getValue().getNodeCase() == NodeCase.EXIT)
                        .collect(Collectors.toList());

        assertEquals(exitNodes.size(), 1);
        Node exitNode = exitNodes.get(0).getValue();
        assertThat(exitNode.getExit().hasReturnContent()).isTrue();
        assertThat(exitNode.getExit().getReturnContent().hasNodeOutput()).isTrue();
        assertEquals(exitNode.getExit().getReturnContent().getNodeOutput().getNodeName(), "1-some-task-TASK");
    }

    public static class UUIDConfigAdapter implements LHStringAdapter<UUID> {

        @Override
        public String toString(UUID src) {
            return src.toString();
        }

        @Override
        public UUID fromString(String src) {
            return UUID.fromString(src);
        }

        @Override
        public Class<UUID> getTypeClass() {
            return UUID.class;
        }
    }

    private static class UUIDIntegerAdapter extends LHIntegerAdapter<UUID> {
        @Override
        public Integer toInteger(UUID src) {
            return src.hashCode();
        }

        @Override
        public UUID fromInteger(Integer src) {
            return new UUID(0, src);
        }

        @Override
        public Class<UUID> getTypeClass() {
            return UUID.class;
        }
    }
}
