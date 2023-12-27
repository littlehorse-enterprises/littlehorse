package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.util.TaskDefUtil;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.TaskDefBuilder;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@LHTest
public class TaskDefIdempotencyTest {

    private LittleHorseBlockingStub client;

    @Test
    void shouldBeIdempotent() {
        TaskDefBuilder task = new TaskDefBuilder(new TaskWorker(), "greet");
        TaskDefBuilder taskCopy = new TaskDefBuilder(new TaskWorker(), "greet");
        TaskDef original = client.putTaskDef(task.toPutTaskDefRequest());
        TaskDef copy = client.putTaskDef(taskCopy.toPutTaskDefRequest());
        assertThat(TaskDefUtil.equals(TaskDefModel.fromProto(original, null), TaskDefModel.fromProto(copy, null)))
                .isTrue();
    }

    @Test
    void shouldThrowAlreadyExistWhenTaskDefDifferent() {
        TaskDefBuilder task = new TaskDefBuilder(new TaskWorker(), "greet-with-update");
        client.putTaskDef(task.toPutTaskDefRequest());

        TaskDefBuilder taskUpdated = new TaskDefBuilder(new TaskWorkerUpdated(), "greet-with-update");

        assertThatThrownBy(() -> client.putTaskDef(taskUpdated.toPutTaskDefRequest()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("ALREADY_EXISTS: TaskDef already exists and is immutable.");
    }

    @Test
    void shouldBeAbleToReadTaskDefImmediatelyAfterCreation() {
        // Repeat 5 times to make race conditions more likely to appear
        for (int i = 0; i < 5; i++) {
            String taskDefName = UUID.randomUUID().toString();
            String wfSpecName = UUID.randomUUID().toString();
            PutTaskDefRequest ptd =
                    PutTaskDefRequest.newBuilder().setName(taskDefName).build();
            PutWfSpecRequest putWfSpec = Workflow.newWorkflow(wfSpecName, wf -> {
                        wf.execute(taskDefName);
                    })
                    .compileWorkflow();

            // Calling these two in immediate succession should guarantee that it works.
            client.putTaskDef(ptd);
            client.putWfSpec(putWfSpec);

            // Note: LH Server doesn't guarantee that the results are immediately available to `get()`.

            // But we can delete them immediately
            client.deleteWfSpec(DeleteWfSpecRequest.newBuilder()
                    .setId(WfSpecId.newBuilder().setName(wfSpecName))
                    .build());
            client.deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                    .setId(TaskDefId.newBuilder().setName(taskDefName))
                    .build());

            // Now make sure we can't create the WfSpec
            assertThatThrownBy(() -> client.putWfSpec(putWfSpec))
                    .matches((exn) ->
                            ((StatusRuntimeException) exn).getStatus().getCode().equals(Code.INVALID_ARGUMENT));
        }
    }
}

class TaskWorker {
    @LHTaskMethod("greet")
    public String greeting(String name) {
        return "hello there, " + name;
    }

    @LHTaskMethod("greet-with-update")
    public String greetingUpdated(String name) {
        return "hello there, " + name;
    }
}

class TaskWorkerUpdated {
    @LHTaskMethod("greet-with-update")
    public String greetingUpdated(String name, String lastName) {
        return "hello there, " + name;
    }
}
