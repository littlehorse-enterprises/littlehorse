package io.littlehorse.server.auth;

import static org.mockito.Mockito.mock;

import com.google.common.collect.Iterables;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptors;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import java.util.Collection;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RequestSanitizerTest {

    private RequestSanitizer sanitizer = new RequestSanitizer();
    private TrackableInterceptor trackableInterceptor = new TrackableInterceptor();
    private final ServerServiceDefinition testServiceDefinition = buildTestServiceDefinition(
            ServerServiceDefinition.builder(LittleHorseGrpc.getServiceDescriptor()),
            LittleHorseGrpc.getServiceDescriptor().getMethods());

    private ServerCall<Object, Object> mockCall = mock();
    private final Metadata requestHeaders = new Metadata();

    @Test
    public void shouldRemoveInternalHeader() {
        requestHeaders.put(ServerAuthorizer.CLIENT_ID, "root");
        requestHeaders.put(ServerAuthorizer.TENANT_ID, "my-tenant");
        startCall();
        Metadata resolvedHeaders = trackableInterceptor.getHeaders();
        Assertions.assertThat(resolvedHeaders).isNotNull();
        Assertions.assertThat(resolvedHeaders.get(ServerAuthorizer.CLIENT_ID)).isNull();
        Assertions.assertThat(resolvedHeaders.get(ServerAuthorizer.TENANT_ID)).isEqualTo("my-tenant");
    }

    private void startCall() {
        ServerServiceDefinition intercept =
                ServerInterceptors.intercept(testServiceDefinition, sanitizer, trackableInterceptor);
        @SuppressWarnings("unchecked")
        ServerMethodDefinition<Object, Object> def =
                (ServerMethodDefinition<Object, Object>) Iterables.get(intercept.getMethods(), 0);
        def.getServerCallHandler().startCall(mockCall, requestHeaders);
    }

    private ServerServiceDefinition buildTestServiceDefinition(
            ServerServiceDefinition.Builder definitionBuilder, Collection<MethodDescriptor<?, ?>> stubMethods) {
        for (MethodDescriptor<?, ?> method : stubMethods) {
            definitionBuilder = definitionBuilder.addMethod(method, (call, headers) -> null);
        }
        return definitionBuilder.build();
    }

    @Getter
    private static class TrackableInterceptor implements ServerAuthorizer {
        private Metadata headers;

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            this.headers = headers;
            return Contexts.interceptCall(Context.current(), call, headers, next);
        }
    }
}
