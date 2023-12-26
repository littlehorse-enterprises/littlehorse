package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.test.LHTest;
import org.junit.jupiter.api.Test;

@LHTest
public class UserTaskIdempotencyTest {

    private LittleHorseBlockingStub client;

    @Test
    void shouldBeIdempotent() {
        String taskName = "sample-task";

        UserTaskSchema schema = new UserTaskSchema(new SampleForm(), taskName);

        UserTaskDef original = client.putUserTaskDef(schema.compile());
        UserTaskDef copy = client.putUserTaskDef(schema.compile());

        assertThat(original.getVersion()).isEqualTo(0);
        assertThat(copy.getVersion()).isEqualTo(original.getVersion());
    }

    @Test
    void shouldCreateNewVersion() {
        String taskName = "updated-task";

        UserTaskSchema schema = new UserTaskSchema(new SampleForm(), taskName);

        UserTaskDef original = client.putUserTaskDef(schema.compile());

        UserTaskSchema newSchema = new UserTaskSchema(new SampleFormUpdated(), taskName);
        UserTaskDef copy = client.putUserTaskDef(newSchema.compile());

        assertThat(original.getVersion()).isEqualTo(0);
        assertThat(copy.getVersion()).isEqualTo(original.getVersion() + 1);
    }
}

class SampleForm {

    @UserTaskField(displayName = "Approved?", description = "Reply 'true' if this is an acceptable request.")
    public boolean isApproved;
}

class SampleFormUpdated {
    @UserTaskField(displayName = "Approved?", description = "Reply 'true' if this is an acceptable request.")
    public boolean isApproved;

    @UserTaskField(displayName = "Approved By?", description = "Put your name")
    public String approvedBy;
}
