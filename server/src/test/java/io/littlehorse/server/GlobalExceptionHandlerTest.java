package io.littlehorse.server;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.exceptions.LHApiException;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

class GlobalExceptionHandlerTest {

    private final ServerCall.Listener<Object> listener = mock();
    private final ServerCall<Object, Object> serverCall = mock();
    private final ServerCallHandler<Object, Object> next = mock();
    private final Metadata metadata = mock(Metadata.class);
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @BeforeEach
    public void setUp() {
        when(next.startCall(serverCall, metadata)).thenReturn(listener);
    }

    @ParameterizedTest
    @EnumSource(Status.Code.class)
    public void shouldHandleLHValidationExceptions(Status.Code expectedCode) {
        String expectedMessage = "test exception " + expectedCode;
        Status expectedStatus = Status.fromCode(expectedCode).withDescription(expectedMessage);

        doThrow(new LHApiException(expectedCode.toStatus(), expectedMessage))
                .when(listener)
                .onHalfClose();
        globalExceptionHandler.interceptCall(serverCall, metadata, next).onHalfClose();
        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
        verify(serverCall).close(statusCaptor.capture(), same(metadata));
        Status actualStatus = statusCaptor.getValue();
        Assertions.assertThat(actualStatus.getCode()).isEqualTo(expectedStatus.getCode());
        Assertions.assertThat(actualStatus.getDescription()).isEqualTo(expectedStatus.getDescription());
        Assertions.assertThat(actualStatus.getCause()).isEqualTo(expectedStatus.getCause());
    }

    @Test
    public void shouldHandleRuntimeExceptions() {
        doThrow(new NullPointerException("oops!")).when(listener).onHalfClose();
        globalExceptionHandler.interceptCall(serverCall, metadata, next).onHalfClose();
        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
        verify(serverCall).close(statusCaptor.capture(), same(metadata));
        Status actualStatus = statusCaptor.getValue();
        Assertions.assertThat(actualStatus.getCode()).isEqualTo(Status.Code.INTERNAL);
        Assertions.assertThat(actualStatus.getDescription()).isEqualTo(GlobalExceptionHandler.INTERNAL_ERROR_MESSAGE);
        Assertions.assertThat(actualStatus.getCause()).isNull();
    }

    @Test
    public void shouldHandleInvalidStateExceptions() {
        InvalidStateStoreException invalidStateException = new InvalidStateStoreException("rebalancing");
        doThrow(invalidStateException).when(listener).onHalfClose();
        globalExceptionHandler.interceptCall(serverCall, metadata, next).onHalfClose();
        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
        verify(serverCall).close(statusCaptor.capture(), same(metadata));
        Status actualStatus = statusCaptor.getValue();
        Assertions.assertThat(actualStatus.getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        Assertions.assertThat(actualStatus.getDescription()).isEqualTo("rebalancing");
        Assertions.assertThat(actualStatus.getCause()).isEqualTo(invalidStateException);
    }

    @Test
    public void shouldHandleStatusRuntimeExceptions() {
        doThrow(new StatusRuntimeException(Status.NOT_FOUND)).when(listener).onHalfClose();
        globalExceptionHandler.interceptCall(serverCall, metadata, next).onHalfClose();
        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
        verify(serverCall).close(statusCaptor.capture(), same(metadata));
        Status actualStatus = statusCaptor.getValue();
        Assertions.assertThat(actualStatus.getCode()).isEqualTo(Status.Code.NOT_FOUND);
        Assertions.assertThat(actualStatus.getDescription()).isEqualTo("not found");
        Assertions.assertThat(actualStatus.getCause()).isNull();
    }
}
