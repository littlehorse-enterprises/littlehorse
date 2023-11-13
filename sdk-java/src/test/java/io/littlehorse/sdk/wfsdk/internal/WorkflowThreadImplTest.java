package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.SleepNode.SleepLengthCase;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
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
        List<VariableDef> varDefs =
                wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName()).getVariableDefsList();

        VariableDef intVar = varDefs.get(0);
        assertThat(intVar.getDefaultValue()).isNotNull();
        assertEquals(intVar.getDefaultValue().getInt(), 123);

        VariableDef objVar = varDefs.get(1);
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
}
