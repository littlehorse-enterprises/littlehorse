package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.FailureHandlerDef.FailureToCatchCase;
import io.littlehorse.sdk.common.proto.FailureHandlerDef.LHFailureType;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.SleepNode.SleepLengthCase;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTATask;
import io.littlehorse.sdk.common.proto.UserTaskNode;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode.ThreadsToWaitForCase;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

public class WorkflowThreadImplTest {

    Faker faker = new Faker();

    @Test
    void testSleepUntil() {
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            WfRunVariable var = thread.addVariable("my-var", VariableType.INT);
            thread.sleepUntil(var);
        });

        PutWfSpecRequest wfSpec = wf.compileWorkflow();

        Node sleepNode = wfSpec.getThreadSpecsOrThrow("entrypoint").getNodesOrThrow("1-sleep-SLEEP");

        assertThat(sleepNode.getSleep()).isNotNull();
        assertEquals(sleepNode.getSleep().getSleepLengthCase(), SleepLengthCase.TIMESTAMP);
        assertEquals(sleepNode.getSleep().getTimestamp().getVariableName(), "my-var");
    }

    @Test
    void testEarlyReturn() {
        WorkflowImpl workflow = new WorkflowImpl("asdf", wf -> {
            wf.execute("some-task");
            wf.fail("some-exception", "some error message");
        });

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
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            WfRunVariable var = thread.addVariable("my-var", VariableType.INT);
            thread.doIf(thread.condition(var, Comparator.LESS_THAN, 10), ifBody -> {
                ifBody.execute("foo");
                ifBody.complete();
            });
            thread.execute("bar");
        });

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
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            thread.addVariable("int-var", 123);
            thread.addVariable("object-var", new Foo("asdf", "fdsa"));
        });

        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        List<ThreadVarDef> varDefs =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getVariableDefsList();

        VariableDef intVar = varDefs.get(0).getVarDef();
        assertThat(intVar.getDefaultValue()).isNotNull();
        assertEquals(intVar.getDefaultValue().getInt(), 123);

        VariableDef objVar = varDefs.get(1).getVarDef();
        assertThat(objVar.getDefaultValue()).isNotEqualTo(null);
        assertEquals(objVar.getType(), VariableType.JSON_OBJ);
    }

    @Test
    void testWhileLoopConditional() {
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            thread.execute("asdf");
            thread.doWhile(thread.condition("asf", Comparator.EQUALS, "asf"), loop -> {
                loop.execute("fdsa");
            });
        });

        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        Node lastNodeInLoopBody =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("2-nop-NOP");

        assertThat(lastNodeInLoopBody.getOutgoingEdgesCount()).isEqualTo(2);
    }

    @Test
    void noRetriesByDefault() {
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            thread.execute("asdf");
        });
        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        Node taskNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("1-asdf-TASK");

        assertThat(taskNode.getTask().getRetries()).isEqualTo(0);
    }

    @Test
    void setIndividualRetry() {
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            thread.execute("asdf").withRetries(2);
            ;
        });
        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        Node taskNode =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getNodesOrThrow("1-asdf-TASK");

        assertThat(taskNode.getTask().getRetries()).isEqualTo(2);
    }

    @Test
    void setDefaultRetryAndOverride() {
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            thread.execute("asdf");
            thread.execute("asdf").withRetries(2);
        });
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
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            thread.execute("asdf");
            thread.execute("asdf").withRetries(2);
            thread.execute("asdf")
                    .withRetries(42)
                    .withExponentialBackoff(ExponentialBackoffRetryPolicy.newBuilder()
                            .setBaseIntervalMs(500)
                            .setMultiplier(137)
                            .setMaxDelayMs(100000)
                            .build());
        });
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
    void setDefaultTaskTimeout() {
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            thread.execute("asdf");
            thread.execute("asdf").timeout(42);
        });
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
        WorkflowImpl wf = new WorkflowImpl("some-wf", thread -> {
            thread.execute("some-task");
        });
        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        assertThat(wfSpec.hasRetentionPolicy()).isFalse();

        assertThat(wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName())
                        .hasRetentionPolicy())
                .isFalse();
    }

    @Test
    void testWfRetentionPolicy() {
        Workflow wf = new WorkflowImpl("some-wf", thread -> {
                    thread.execute("some-task");
                })
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
        Workflow wf = new WorkflowImpl("some-wf", thread -> {
                    thread.execute("some-task");
                })
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
        Workflow wf = new WorkflowImpl("some-wf", thread -> {
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
                })
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
        Workflow wf = new WorkflowImpl("some-wf", thread -> {
            thread.waitForEvent("some-event").timeout(10);
        });

        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        ThreadSpec entrypoint = wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName());
        Node node = entrypoint.getNodesOrThrow("1-some-event-EXTERNAL_EVENT");
        assertThat(node.getExternalEvent().getTimeoutSeconds().getLiteralValue().getInt())
                .isEqualTo(10);
    }

    @Test
    void testWaitForThreadsHandleAnyFailureOnChild() {
        Workflow workflow = new WorkflowImpl("some-wf", wf -> {
            SpawnedThread child = wf.spawnThread(childThread -> {}, "child", Map.of());
            WaitForThreadsNodeOutput result = wf.waitForThreads(SpawnedThreads.of(child));
            result.handleAnyFailureOnChild(handler -> {});
        });
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
        Workflow workflow = new WorkflowImpl("some-wf", wf -> {
            SpawnedThread child = wf.spawnThread(childThread -> {}, "child", Map.of());
            WaitForThreadsNodeOutput result = wf.waitForThreads(SpawnedThreads.of(child));
            result.handleErrorOnChild(LHErrorType.TIMEOUT, handler -> {});
            result.handleErrorOnChild(null, handler -> {});
        });
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
        Workflow workflow = new WorkflowImpl("some-wf", wf -> {
            SpawnedThread child = wf.spawnThread(childThread -> {}, "child", Map.of());
            WaitForThreadsNodeOutput result = wf.waitForThreads(SpawnedThreads.of(child));
            result.handleExceptionOnChild("my-exception", handler -> {});
            result.handleExceptionOnChild(null, handler -> {});
        });
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
        Workflow workflow = new WorkflowImpl("some-wf", wf -> {
            WfRunVariable toSpawn = wf.addVariable("to-spawn", VariableType.JSON_ARR);
            wf.waitForThreads(wf.spawnThreadForEach(toSpawn, "child", child -> {}));
        });
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
        Workflow workflow = new WorkflowImpl("throw-event-wf", wf -> {
            WfRunVariable var = wf.addVariable("my-var", VariableType.STR);
            wf.throwEvent("my-event", var);
            wf.throwEvent("another-event", "some-content");
        });
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
        Workflow workflow = new WorkflowImpl("throw-event-wf", wf -> {
            UserTaskOutput uto = wf.assignUserTask("some-usertaskdef", "some-person", "some-group");
            wf.scheduleReminderTask(uto, 10, "some-taskdef");
        });
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
}
