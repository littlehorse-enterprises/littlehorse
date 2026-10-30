package io.littlehorse.common.model.corecommand.subcommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.sdk.common.proto.PutVariableRequest;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PutVariableRequestModelTest {

    @ParameterizedTest
    @MethodSource("malformedJson")
    void shouldRejectJsonDeserializationErrors(VariableValue value) {
        PutVariableRequest request = PutVariableRequest.newBuilder()
                .setId(VariableId.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId("test-run"))
                        .setName("value")
                        .setThreadRunNumber(0))
                .setValue(value)
                .build();

        LHApiException error = assertThrows(
                LHApiException.class, () -> LHSerializable.fromProto(request, PutVariableRequestModel.class, null));

        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(error.getStatus().getDescription()).contains("Error deserializing JSON");
    }

    private static Stream<VariableValue> malformedJson() {
        return Stream.of(
                VariableValue.newBuilder().setJsonObj("{\"a\":").build(),
                VariableValue.newBuilder().setJsonArr("[1,").build());
    }
}
