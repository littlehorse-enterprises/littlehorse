package io.littlehorse.sdk.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Any;
import com.google.protobuf.util.Durations;
import com.google.rpc.Code;
import com.google.rpc.RetryInfo;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.StatusProto;
import io.littlehorse.sdk.common.config.retryinterceptor.ResourceExhaustedRetryInterceptor;
import org.junit.jupiter.api.Test;

class ResourceExhaustedRetryInterceptorTest {

    @Test
    void shouldExtractRetryDelayFromStatusDetails() {
        RetryInfo retryInfo =
                RetryInfo.newBuilder().setRetryDelay(Durations.fromMillis(1500)).build();
        com.google.rpc.Status statusDetails = com.google.rpc.Status.newBuilder()
                .setCode(Code.RESOURCE_EXHAUSTED.getNumber())
                .addDetails(Any.pack(retryInfo))
                .build();
        Metadata trailers = StatusProto.toStatusRuntimeException(statusDetails).getTrailers();

        Long retryDelayMillis =
                ResourceExhaustedRetryInterceptor.getRetryDelayMillis(Status.RESOURCE_EXHAUSTED, trailers);

        assertThat(retryDelayMillis).isEqualTo(1500L);
    }

    @Test
    void shouldIgnoreStatusesWithoutRetryInfo() {
        Metadata trailers = new Metadata();

        Long retryDelayMillis =
                ResourceExhaustedRetryInterceptor.getRetryDelayMillis(Status.RESOURCE_EXHAUSTED, trailers);

        assertThat(retryDelayMillis).isNull();
    }

    @Test
    void shouldIgnoreNonQuotaStatuses() {
        RetryInfo retryInfo =
                RetryInfo.newBuilder().setRetryDelay(Durations.fromMillis(1500)).build();
        com.google.rpc.Status statusDetails = com.google.rpc.Status.newBuilder()
                .setCode(Code.RESOURCE_EXHAUSTED.getNumber())
                .addDetails(Any.pack(retryInfo))
                .build();
        Metadata trailers = StatusProto.toStatusRuntimeException(statusDetails).getTrailers();

        Long retryDelayMillis = ResourceExhaustedRetryInterceptor.getRetryDelayMillis(Status.UNAVAILABLE, trailers);

        assertThat(retryDelayMillis).isNull();
    }
}
