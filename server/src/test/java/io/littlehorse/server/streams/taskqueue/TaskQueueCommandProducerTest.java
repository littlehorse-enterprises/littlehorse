package io.littlehorse.server.streams.taskqueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.MockLHProducer;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunSourceModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskQueueCommandProducerTest {

    private final MockLHProducer autoCompleteMockProducer = MockLHProducer.create();
    private final MockLHProducer mockProducer = MockLHProducer.create(false);
    private final String commandTopic = "test";
    private final PollTaskRequestObserver requestObserver = mock();
    private final ProcessorExecutionContext processorContext = mock();
    private final AuthorizationContext mockAuthContext = mock();
    private final StreamObserver<Empty> reportTaskRunObserver = mock();

    @BeforeEach
    void setup() {
        when(requestObserver.getPrincipalId()).thenReturn(new PrincipalIdModel("test-principal"));
        when(requestObserver.getTenantId()).thenReturn(new TenantIdModel("test-tenant"));
        when(requestObserver.getTaskWorkerVersion()).thenReturn("1.0.0");
        when(requestObserver.getClientId()).thenReturn("test-worker");
        when(mockAuthContext.principalId()).thenReturn(new PrincipalIdModel("test-principal"));
        when(mockAuthContext.tenantId()).thenReturn(new TenantIdModel("test-tenant"));
    }

    @Test
    void shouldReturnTaskToClientAfterProducerRecordSent() {
        final TaskQueueCommandProducer taskQueueProducer =
                new TaskQueueCommandProducer(autoCompleteMockProducer, commandTopic);
        ScheduledTaskModel scheduledTask = buildScheduledTask();
        taskQueueProducer.returnTaskToClient(scheduledTask, requestObserver);
        verify(requestObserver).sendResponse(scheduledTask);
    }

    @Test
    void shouldCloseResponseObserverWithErrorOnProducerFailures() {
        final RuntimeException expectedException = new RuntimeException("oops");
        final TaskQueueCommandProducer taskQueueProducer = new TaskQueueCommandProducer(mockProducer, commandTopic);
        ScheduledTaskModel scheduledTask = buildScheduledTask();
        taskQueueProducer.returnTaskToClient(scheduledTask, requestObserver);
        mockProducer.getKafkaProducer().errorNext(expectedException);
        verify(requestObserver).onError(expectedException);
    }

    @Test
    void shouldSendReportTaskRunCommands() {
        final TaskQueueCommandProducer taskQueueProducer =
                new TaskQueueCommandProducer(autoCompleteMockProducer, commandTopic);
        ReportTaskRunModel reportTaskRun = buildReportTaskRunModel();
        taskQueueProducer.send(reportTaskRun, mockAuthContext, reportTaskRunObserver);
        verify(reportTaskRunObserver).onNext(any());
        verify(reportTaskRunObserver).onCompleted();
    }

    private ScheduledTaskModel buildScheduledTask() {
        final TaskDefIdModel taskDefId = new TaskDefIdModel("task-1");
        final WfRunIdModel wfRunId = new WfRunIdModel("wf-run-1");
        final TaskRunIdModel taskRunId = new TaskRunIdModel(wfRunId, "task-run-1");
        ScheduledTaskModel scheduledTask = new ScheduledTaskModel();
        scheduledTask.setVariables(List.of());
        scheduledTask.setAttemptNumber(1);
        scheduledTask.setCreatedAt(new Date());
        scheduledTask.setSource(new TaskRunSourceModel());
        scheduledTask.setTaskDefId(taskDefId);
        scheduledTask.setTaskRunId(taskRunId);
        return scheduledTask;
    }

    private ReportTaskRunModel buildReportTaskRunModel() {
        ReportTaskRun reportTaskRun = ReportTaskRun.newBuilder()
                .setAttemptNumber(1)
                .setOutput(VariableValue.newBuilder().setInt(10))
                .setTaskRunId(TaskRunId.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId("test"))
                        .setTaskGuid("test-guid"))
                .setTime(LHLibUtil.fromDate(new Date()))
                .build();
        return LHSerializable.fromProto(reportTaskRun, ReportTaskRunModel.class, processorContext);
    }
}
