package io.littlehorse.sdk.usertask;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.StructDefId;
import org.junit.jupiter.api.Test;

class UserTaskSchemaTest {

    @Test
    void shouldCompileStructBackedUserTaskDef() {
        StructDefId structDefId = StructDefId.newBuilder()
                .setName("approval-result")
                .setVersion(2)
                .build();

        PutUserTaskDefRequest result = new UserTaskSchema(structDefId, "approve-request").compile();

        assertThat(result.getName()).isEqualTo("approve-request");
        assertThat(result.getResultStructDefId()).isEqualTo(structDefId);
        assertThat(result.getFieldsCount()).isZero();
    }
}
