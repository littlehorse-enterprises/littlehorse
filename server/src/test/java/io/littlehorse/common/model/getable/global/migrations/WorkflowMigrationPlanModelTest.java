package io.littlehorse.common.model.getable.global.migrations;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowMigrationPlanIdModel;
import io.littlehorse.sdk.common.proto.WorkflowMigrationPlan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

@ExtendWith(MockitoExtension.class)
public class WorkflowMigrationPlanModelTest {

    // A deep-stub mock satisfies any chained method call (e.g. context.service().getX())
    // without requiring explicit stubbing. It is the standard pattern in this codebase.
    private final ExecutionContext mockContext = mock(Answers.RETURNS_DEEP_STUBS);

    @Test
    public void shouldRoundTripToAndFromProto() {
        // --- Arrange ---
        // Build the thread migration that will be nested inside the plan.
        ThreadMigrationPlanModel threadMigration = new ThreadMigrationPlanModel();
        threadMigration.setNewThreadName("entrypoint");
        NodeMigrationPlanModel nodeMigration = new NodeMigrationPlanModel();
        nodeMigration.setNewNodeName("new-task-node");
        threadMigration.setNodeMigrations(Map.of("old-task-node", nodeMigration));
        threadMigration.setRequiredVariables(List.of("my-variable"));
        threadMigration.setDependencies(List.of("dep-thread"));

        // Build the top-level migration plan model.
        WorkflowMigrationPlanModel original = new WorkflowMigrationPlanModel(
                new WorkflowMigrationPlanIdModel("my-migration-plan"),
                new Date(),
                Map.of("entrypoint", threadMigration),
                new WfSpecIdModel("my-wf", 0, 0), // oldWfSpec: name, majorVersion, revision
                0,  // majorVersion of the new WfSpec to migrate to
                1); // revision of the new WfSpec to migrate to

        // --- Act ---
        // Serialize to proto then deserialize back. This is the standard round-trip pattern.
        WorkflowMigrationPlan proto = original.toProto().build();
        WorkflowMigrationPlanModel deserialized =
                LHSerializable.fromProto(proto, WorkflowMigrationPlanModel.class, mockContext);

        // --- Assert ---
        assertThat(deserialized.getId().getName()).isEqualTo("my-migration-plan");
        assertThat(deserialized.getOldWfSpecId().getName()).isEqualTo("my-wf");
        assertThat(deserialized.getOldWfSpecId().getMajorVersion()).isEqualTo(0);
        assertThat(deserialized.getMajorVersion()).isEqualTo(0);
        assertThat(deserialized.getRevision()).isEqualTo(1);

        ThreadMigrationPlanModel deserializedThread =
                deserialized.getThreadMigrations().get("entrypoint");
        assertThat(deserializedThread).isNotNull();
        assertThat(deserializedThread.getNewThreadName()).isEqualTo("entrypoint");
        assertThat(deserializedThread.getNodeMigrations()).containsKey("old-task-node");
        assertThat(deserializedThread.getNodeMigrations().get("old-task-node").getNewNodeName())
                .isEqualTo("new-task-node");
        assertThat(deserializedThread.getRequiredVariables()).containsExactly("my-variable");
        assertThat(deserializedThread.getDependencies()).containsExactly("dep-thread");
    }
}
