package io.littlehorse.sdk.common.config;

import static io.littlehorse.sdk.common.ConcurrentTestSupport.runConcurrently;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class LHConfigConcurrencyTest {
    @Test
    void sharesChannelsAcrossConcurrentStubTypesAndSeparatesEndpoints() throws Exception {
        LHConfig config = new LHConfig(Map.of(LHConfig.GRPC_RESOURCE_EXHAUSTED_RETRY_KEY, "false"));
        Set<Channel> channels = ConcurrentHashMap.newKeySet();
        AtomicInteger calls = new AtomicInteger();
        try {
            var results = runConcurrently(24, () -> {
                int call = calls.getAndIncrement();
                int port = call % 2 == 0 ? 2023 : 2024;
                Channel channel =
                        switch (call % 3) {
                            case 0 -> config.getBlockingStub("localhost", port).getChannel();
                            case 1 -> config.getAsyncStub("localhost", port).getChannel();
                            default -> config.getFutureStub("localhost", port).getChannel();
                        };
                channels.add(channel);
                return channel;
            });
            assertThat(new HashSet<>(results)).hasSize(2);
            assertThat(config.getBlockingStub("localhost", 2023).getChannel())
                    .isSameAs(config.getAsyncStub("localhost", 2023).getChannel());
            assertThat(config.getBlockingStub("localhost", 2024).getChannel())
                    .isNotSameAs(config.getBlockingStub("localhost", 2023).getChannel());
        } finally {
            channels.forEach(channel -> ((ManagedChannel) channel).shutdownNow());
        }
    }

    @Test
    void builderReuseDoesNotChangePreviouslyBuiltInterceptors() {
        var builder = LHConfig.newBuilder();
        LHConfig config = builder.build();
        builder.addClientInterceptor(new io.grpc.ClientInterceptor() {
            @Override
            public <ReqT, RespT> io.grpc.ClientCall<ReqT, RespT> interceptCall(
                    io.grpc.MethodDescriptor<ReqT, RespT> method, io.grpc.CallOptions options, Channel next) {
                throw new AssertionError("Interceptor added after build must not reach this config");
            }
        });
        ManagedChannel managed = mock();
        ManagedChannelBuilder<?> channelBuilder = mock(ManagedChannelBuilder.class, RETURNS_SELF);
        when(channelBuilder.build()).thenReturn(managed);
        try (MockedStatic<ManagedChannelBuilder> factory = mockStatic(ManagedChannelBuilder.class)) {
            factory.when(() -> ManagedChannelBuilder.forAddress("localhost", 2023))
                    .thenReturn(channelBuilder);
            Channel channel = config.getBlockingStub().getChannel();
            assertThat(channel.newCall(
                            io.littlehorse.sdk.common.proto.LittleHorseGrpc.getGetTaskDefMethod(),
                            io.grpc.CallOptions.DEFAULT))
                    .isNotNull();
        }
    }

    @Test
    void failedChannelCreationRemainsRetryable() {
        AtomicInteger attempts = new AtomicInteger();
        LHConfig config = new LHConfig(Map.of(LHConfig.GRPC_RESOURCE_EXHAUSTED_RETRY_KEY, "false")) {
            @Override
            public String getApiProtocol() {
                if (attempts.getAndIncrement() == 0) {
                    throw new IllegalArgumentException("temporary configuration failure");
                }
                return super.getApiProtocol();
            }
        };
        assertThatThrownBy(config::getBlockingStub).hasMessage("temporary configuration failure");
        ManagedChannel channel = (ManagedChannel) config.getBlockingStub().getChannel();
        try {
            assertThat(config.getBlockingStub().getChannel()).isSameAs(channel);
        } finally {
            channel.shutdownNow();
        }
    }
}
