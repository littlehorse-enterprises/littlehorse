package io.littlehorse.common.model.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.littlehorse.TestUtil;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEventModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.stores.PartitionMetricsMemoryStore;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.GetableUpdates;
import java.util.ArrayList;
import java.util.Date;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

public class TaskRunModelTest {

    private final String tenantId = "myTenantId";
    private final ExecutionContext executionContext = mock();
    private final CoreProcessorContext processorContext = mock(Answers.RETURNS_DEEP_STUBS);

    @Test
    void setTaskWorkerVersionAndIdToTaskRun() {
        // arrange. Complex because all the dependencies needed
        TaskRun taskRunProto = TaskRun.newBuilder()
                .setId(new TaskRunIdModel(new NodeRunIdModel(new WfRunIdModel("asdf"), 0, 1), processorContext)
                        .toProto())
                .addAttempts(TaskAttempt.newBuilder().setStatus(TaskStatus.TASK_PENDING))
                .build();
        when(executionContext.castOnSupport(CoreProcessorContext.class)).thenReturn(processorContext);

        TaskRunModel taskRun = TaskRunModel.fromProto(taskRunProto, executionContext);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        AuthorizationContext mockContext = mock(AuthorizationContext.class);
        when(mockContext.tenantId()).thenReturn(new TenantIdModel(tenantId));
        when(executionContext.authorization()).thenReturn(mockContext);
        taskRun.setInputVariables(new ArrayList<>());

        taskRun.dispatchTaskToQueue();
        verify(processorContext.getTaskManager()).scheduleTask(any());

        TaskClaimEventModel taskClaimEvent = new TaskClaimEventModel();
        // act
        taskRun.onTaskAttemptStarted(taskClaimEvent);

        // assert
        assertThat(taskRun.getLatestAttempt().getTaskWorkerVersion()).isEqualTo(taskClaimEvent.getTaskWorkerVersion());
        assertThat(taskRun.getLatestAttempt().getTaskWorkerId()).isEqualTo(taskClaimEvent.getTaskWorkerId());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldMarkTaskOutputSerdeErrorWhenOutputIncompatibleWithDeclaredReturnType() {
        TaskRunModel taskRun = TestUtil.taskRun();
        taskRun.getLatestAttempt().setStatus(TaskStatus.TASK_RUNNING);

        TaskDefModel taskDef = new TaskDefModel();
        taskDef.setId(new TaskDefIdModel("test-name"));
        taskDef.setReturnType(new ReturnTypeModel(new TypeDefinitionModel(VariableType.INT)));

        ReadOnlyMetadataManager metadataManager = mock(ReadOnlyMetadataManager.class);
        when(metadataManager.get(taskRun.getTaskDefId())).thenReturn(taskDef);

        ExecutionContext taskExecutionContext = mock(ExecutionContext.class, Answers.RETURNS_DEEP_STUBS);
        when(taskExecutionContext.metadataManager()).thenReturn(metadataManager);
        taskRun.setExecutionContext(taskExecutionContext);

        CoreProcessorContext taskProcessorContext = mock(CoreProcessorContext.class);
        GetableManager getableManager = mock(GetableManager.class);
        GetableUpdates getableUpdates = mock(GetableUpdates.class);
        WfRunModel wfRun = mock(WfRunModel.class);
        KeyValueStore<String, Bytes> mockStore = mock(KeyValueStore.class);
        when(taskProcessorContext.getableManager()).thenReturn(getableManager);
        when(getableManager.get(taskRun.getWfRunId())).thenReturn(wfRun);
        when(taskProcessorContext.getableUpdates()).thenReturn(getableUpdates);
        when(taskProcessorContext.authorization()).thenReturn(mock(AuthorizationContext.class));
        when(taskProcessorContext.authorization().tenantId()).thenReturn(new TenantIdModel("tenant-a"));
        when(taskProcessorContext.getPartitionMetricsMemoryStore()).thenReturn(new PartitionMetricsMemoryStore());
        when(taskProcessorContext.nativeCoreStore()).thenReturn(mockStore);
        taskRun.setProcessorContext(taskProcessorContext);

        ReportTaskRunModel report = new ReportTaskRunModel();
        report.setAttemptNumber(0);
        report.setStatus(TaskStatus.TASK_SUCCESS);
        report.setOutput(new VariableValueModel("not-an-int"));
        report.setTime(new Date());
        report.setTotalCheckpoints(0);

        taskRun.onTaskAttemptResultReported(report);

        assertThat(taskRun.getLatestAttempt().getStatus()).isEqualTo(TaskStatus.TASK_OUTPUT_SERDE_ERROR);
        assertThat(taskRun.getLatestAttempt().getError()).isNotNull();
        assertThat(taskRun.getLatestAttempt().getError().getMessage())
                .contains("incompatible with declared return type");
    }
}
