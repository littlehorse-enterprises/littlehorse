package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.test.LHTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@LHTest
public class UserTaskDefLifecycleTest {

    private LittleHorseBlockingStub client;

    @Test
    void shouldBeIdempotent() {
        String taskName = UUID.randomUUID().toString();

        UserTaskSchema schema = new UserTaskSchema(new SampleForm(), taskName);

        UserTaskDef original = client.putUserTaskDef(schema.compile());
        UserTaskDef copy = client.putUserTaskDef(schema.compile());

        assertThat(original.getVersion()).isEqualTo(0);
        assertThat(copy.getVersion()).isEqualTo(original.getVersion());
    }

    @Test
    void shouldCreateNewVersion() {
        String taskName = UUID.randomUUID().toString();

        UserTaskSchema schema = new UserTaskSchema(new SampleForm(), taskName);

        UserTaskDef original = client.putUserTaskDef(schema.compile());

        UserTaskSchema newSchema = new UserTaskSchema(new SampleFormUpdated(), taskName);
        UserTaskDef copy = client.putUserTaskDef(newSchema.compile());

        assertThat(original.getVersion()).isEqualTo(0);
        assertThat(copy.getVersion()).isEqualTo(original.getVersion() + 1);
    }

    @Test
    void shouldCreateIdempotentStructBackedUserTaskDef() {
        StructDef structDef = putStructDef();
        PutUserTaskDefRequest request = PutUserTaskDefRequest.newBuilder()
                .setName("struct-user-task-" + UUID.randomUUID())
                .setResultStructDefId(structDef.getId())
                .build();

        UserTaskDef original = client.putUserTaskDef(request);
        UserTaskDef copy = client.putUserTaskDef(request);

        assertThat(original.getResultStructDefId()).isEqualTo(structDef.getId());
        assertThat(copy.getVersion()).isEqualTo(original.getVersion());
    }

    @Test
    void shouldRejectUnknownResultStructDef() {
        PutUserTaskDefRequest request = PutUserTaskDefRequest.newBuilder()
                .setName("unknown-struct-user-task-" + UUID.randomUUID())
                .setResultStructDefId(StructDefId.newBuilder()
                        .setName("missing-struct-" + UUID.randomUUID())
                        .setVersion(0))
                .build();

        assertThatThrownBy(() -> client.putUserTaskDef(request))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void shouldRejectFieldsAndResultStructDefTogether() {
        StructDef structDef = putStructDef();
        PutUserTaskDefRequest legacy = new UserTaskSchema(new SampleForm(), "ignored").compile();
        PutUserTaskDefRequest request = legacy.toBuilder()
                .setName("conflicting-user-task-" + UUID.randomUUID())
                .setResultStructDefId(structDef.getId())
                .build();

        assertThatThrownBy(() -> client.putUserTaskDef(request))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("cannot define both fields and result_struct_def_id");
    }

    private StructDef putStructDef() {
        return client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("user-task-output-" + UUID.randomUUID())
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "approved",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL))
                                        .build()))
                .build());
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
