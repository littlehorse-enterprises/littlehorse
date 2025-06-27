package io.littlehorse.server.interceptors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

public class RequestBlockerTest {

    private RequestBlocker requestBlocker;
    private ServerCall<Object, Object> mockCall;
    private ServerCallHandler<Object, Object> mockHandler;
    private Metadata metadata;
    private MethodDescriptor<Object, Object> methodDescriptor;

    @BeforeEach
    public void setup() {
        requestBlocker = new RequestBlocker();
        mockCall = mock(Answers.RETURNS_DEEP_STUBS);
        mockHandler = mock();
        metadata = new Metadata();
        methodDescriptor = mock();

        when(mockCall.getMethodDescriptor()).thenReturn(methodDescriptor);
        when(methodDescriptor.getBareMethodName()).thenReturn("TestMethod");
    }

    @Test
    public void shouldAllowRequestsInitially() {
        ServerCall.Listener<Object> mockListener = mock();
        when(mockHandler.startCall(mockCall, metadata)).thenReturn(mockListener);
        ServerCall.Listener<Object> result = requestBlocker.interceptCall(mockCall, metadata, mockHandler);
        verify(mockHandler).startCall(mockCall, metadata);
        assertThat(result).isEqualTo(mockListener);
        verify(mockCall, never()).close(any(Status.class), any(Metadata.class));
    }

    @Test
    public void shouldBlockRequestsAfterShutdownHookIsTriggered() throws Exception {
        triggerShutdownHook();
        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
        ArgumentCaptor<Metadata> metadataCaptor = ArgumentCaptor.forClass(Metadata.class);
        ServerCall.Listener<Object> result = requestBlocker.interceptCall(mockCall, metadata, mockHandler);
        verify(mockCall).close(statusCaptor.capture(), metadataCaptor.capture());
        verify(mockHandler, never()).startCall(any(), any());
        Status capturedStatus = statusCaptor.getValue();
        assertThat(capturedStatus.getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(capturedStatus.getDescription())
                .isEqualTo("Server is shutting down, no new requests are being accepted");
        assertThat(result).isNotNull();
    }

    @Test
    public void shouldTransitionFromAllowModeToBlockMode() throws Exception {
        ServerCall.Listener<Object> mockListener = mock();
        when(mockHandler.startCall(mockCall, metadata)).thenReturn(mockListener);
        ServerCall.Listener<Object> firstResult = requestBlocker.interceptCall(mockCall, metadata, mockHandler);
        verify(mockHandler).startCall(mockCall, metadata);
        assertThat(firstResult).isEqualTo(mockListener);

        triggerShutdownHook();

        ServerCall<Object, Object> secondMockCall = mock(Answers.RETURNS_DEEP_STUBS);
        when(secondMockCall.getMethodDescriptor()).thenReturn(methodDescriptor);
        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);

        requestBlocker.interceptCall(secondMockCall, metadata, mockHandler);

        verify(secondMockCall).close(statusCaptor.capture(), any(Metadata.class));
        Status capturedStatus = statusCaptor.getValue();
        assertThat(capturedStatus.getCode()).isEqualTo(Status.Code.UNAVAILABLE);
    }

    private void triggerShutdownHook() throws Exception {
        // Trigger the shutdown hook manually using reflection
        Method blockRequestsMethod = RequestBlocker.class.getDeclaredMethod("blockRequests");
        blockRequestsMethod.setAccessible(true);
        blockRequestsMethod.invoke(requestBlocker);
    }
}
