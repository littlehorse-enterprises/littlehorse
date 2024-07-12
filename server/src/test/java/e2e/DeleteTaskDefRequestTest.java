package e2e;

import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.SearchTaskDefRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskDefIdList;
import io.littlehorse.sdk.common.proto.TaskWorkerGroup;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class DeleteTaskDefRequestTest {

    @LHWorkflow("test-basic")
    private Workflow basicWf;

    private WorkflowVerifier verifier;

    @Test
    public void shouldDoBasic() {
        verifier.prepareRun(basicWf).waitForStatus(LHStatus.COMPLETED).start();

        SearchTaskDefRequest searchTaskDefRequest =
                SearchTaskDefRequest.newBuilder().build();

        TaskDefIdList taskDefIdList = verifier.getLhClient().searchTaskDef(searchTaskDefRequest);

        TaskDefId taskDefId2 = taskDefIdList.getResults(0);

        Assertions.assertEquals("ag-one", taskDefId2.getName());

        DeleteTaskDefRequest deleteTaskDefRequest =
                DeleteTaskDefRequest.newBuilder().setId(taskDefId2).build();

        TaskWorkerGroup taskWorkerGroup = verifier.getLhClient().getTaskWorkerGroup(taskDefId2);

        verifier.getLhClient().deleteTaskDef(deleteTaskDefRequest);

        TaskWorkerGroup taskWorkerGroup2 = verifier.getLhClient().getTaskWorkerGroup(taskDefId2);

        Assertions.assertNotEquals(taskWorkerGroup, taskWorkerGroup2);

        // System.out.println("TaskWorkerGroup: " + taskWorkerGroup.getId());

        // Assertions.assertEquals(5, 5);;
    }

    @LHWorkflow("test-basic")
    public Workflow getBasic() {
        return new WorkflowImpl("test-basic", thread -> {
            thread.execute("ag-one");
        });
    }

    @LHTaskMethod("ag-one")
    public boolean one() {
        return true;
    }
}
