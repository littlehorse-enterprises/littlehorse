package e2e;

import io.littlehorse.TestUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.test.LHTest;
import java.util.Date;
import org.junit.jupiter.api.Test;

@LHTest
public class VarSerdeTest {
    public LittleHorseBlockingStub client;

    @Test
    public void shouldNotFailWhenReportTaskRunHasMalformedJsonObj() {
        client.reportTask(ReportTaskRun.newBuilder()
                .setTaskRunId(TestUtil.taskRunId().toProto())
                .setTime(LHLibUtil.fromDate(new Date()))
                .setStatus(TaskStatus.TASK_SUCCESS)
                .setAttemptNumber(0)
                .setOutput(VariableValue.newBuilder().setJsonObj("Test").build())
                .build());
    }

    @Test
    public void shouldNotFailWhenReportTaskRunHasMalformedJsonArr() {
        client.reportTask(ReportTaskRun.newBuilder()
                .setTaskRunId(TestUtil.taskRunId().toProto())
                .setTime(LHLibUtil.fromDate(new Date()))
                .setStatus(TaskStatus.TASK_SUCCESS)
                .setAttemptNumber(0)
                .setOutput(VariableValue.newBuilder().setJsonArr("Test").build())
                .build());
    }

    @Test
    public void shouldAcceptUtcTimestampVariableInReportTask() {
        client.reportTask(ReportTaskRun.newBuilder()
                .setTaskRunId(TestUtil.taskRunId().toProto())
                .setTime(LHLibUtil.fromDate(new Date()))
                .setStatus(TaskStatus.TASK_SUCCESS)
                .setAttemptNumber(0)
                .setOutput(VariableValue.newBuilder()
                        .setUtcTimestamp(LHLibUtil.fromDate(new Date()))
                        .build())
                .build());
    }
}
