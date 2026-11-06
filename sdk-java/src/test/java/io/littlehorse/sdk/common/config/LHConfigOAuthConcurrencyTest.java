package io.littlehorse.sdk.common.config;

import static io.littlehorse.sdk.common.ConcurrentTestSupport.runConcurrently;
import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.grpc.CallCredentials.MetadataApplier;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.littlehorse.sdk.common.proto.TenantId;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LHConfigOAuthConcurrencyTest {
    @Test
    void concurrentStubCreationSharesOAuthTokenAndPreservesTenants() throws Exception {
        AtomicInteger tokenRequests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/token", exchange -> {
            exchange.getRequestBody().close();
            tokenRequests.incrementAndGet();
            byte[] response = "{\"access_token\":\"shared-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var body = exchange.getResponseBody()) {
                body.write(response);
            } finally {
                exchange.close();
            }
        });
        server.start();
        Set<ManagedChannel> channels = ConcurrentHashMap.newKeySet();
        try {
            LHConfig config = new LHConfig(Map.of(
                    LHConfig.GRPC_RESOURCE_EXHAUSTED_RETRY_KEY, "false",
                    LHConfig.OAUTH_CLIENT_ID_KEY, "client",
                    LHConfig.OAUTH_CLIENT_SECRET_KEY, "secret",
                    LHConfig.OAUTH_ACCESS_TOKEN_URL_KEY,
                            "http://127.0.0.1:" + server.getAddress().getPort() + "/token"));
            AtomicInteger tenant = new AtomicInteger();
            var stubs = runConcurrently(16, () -> {
                int number = tenant.getAndIncrement();
                var stub = config.getBlockingStub(
                        "localhost",
                        2023,
                        TenantId.newBuilder().setId("tenant-" + number).build());
                channels.add((ManagedChannel) stub.getChannel());
                return stub;
            });
            assertThat(channels).hasSize(1);
            assertThat(tokenRequests.get()).isZero();
            AtomicInteger index = new AtomicInteger();
            var metadata = runConcurrently(16, () -> {
                var stub = stubs.get(index.getAndIncrement());
                CompletableFuture<Metadata> result = new CompletableFuture<>();
                stub.getCallOptions().getCredentials().applyRequestMetadata(null, Runnable::run, new MetadataApplier() {
                    @Override
                    public void apply(Metadata headers) {
                        result.complete(headers);
                    }

                    @Override
                    public void fail(Status status) {
                        result.completeExceptionally(status.asRuntimeException());
                    }
                });
                return result.get(5, TimeUnit.SECONDS);
            });
            assertThat(tokenRequests.get()).isEqualTo(1);
            for (int i = 0; i < metadata.size(); i++) {
                assertThat(metadata.get(i).get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)))
                        .isEqualTo("Bearer shared-token");
            }
            assertThat(metadata.stream()
                            .map(headers -> headers.get(Metadata.Key.of("tenantId", Metadata.ASCII_STRING_MARSHALLER)))
                            .toList())
                    .containsExactlyInAnyOrderElementsOf(java.util.stream.IntStream.range(0, 16)
                            .mapToObj(i -> "tenant-" + i)
                            .toList());
        } finally {
            channels.forEach(ManagedChannel::shutdownNow);
            server.stop(0);
        }
    }
}
