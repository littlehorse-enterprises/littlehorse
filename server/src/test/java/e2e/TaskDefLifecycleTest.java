package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.util.TaskDefUtil;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.TaskDefBuilder;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import org.junit.jupiter.api.Test;

@LHTest
public class TaskDefLifecycleTest {

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
