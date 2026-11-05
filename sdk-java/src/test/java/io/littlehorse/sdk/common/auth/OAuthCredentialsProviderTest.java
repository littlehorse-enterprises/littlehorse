package io.littlehorse.sdk.common.auth;

import static io.littlehorse.sdk.common.ConcurrentTestSupport.runConcurrently;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.grpc.CallCredentials.MetadataApplier;
import io.grpc.Metadata;
import io.grpc.Status;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OAuthCredentialsProviderTest {
    private static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Test
    void concurrentCallsShareInitialAndRefreshedTokens() throws Exception {
        OAuthClient client = mock();
        TokenStatus first = mock();
        TokenStatus second = mock();
        when(first.isValid()).thenReturn(true);
        when(first.getToken()).thenReturn("first");
        when(second.isValid()).thenReturn(true);
        when(second.getToken()).thenReturn("second");
        when(client.getAccessToken()).thenReturn(first, second);
        OAuthCredentialsProvider provider = new OAuthCredentialsProvider(client);

        assertThat(runConcurrently(16, () -> authorization(provider))).containsOnly("Bearer first");
        verify(client, times(1)).getAccessToken();

        when(first.isValid()).thenReturn(false);
        assertThat(runConcurrently(16, () -> authorization(provider))).containsOnly("Bearer second");
        verify(client, times(2)).getAccessToken();
    }

    @Test
    void metadataCallbacksDoNotHoldTheTokenCacheLock() throws Exception {
        OAuthClient client = mock();
        TokenStatus token = mock();
        when(token.isValid()).thenReturn(true);
        when(token.getToken()).thenReturn("shared");
        when(client.getAccessToken()).thenReturn(token);
        OAuthCredentialsProvider provider = new OAuthCredentialsProvider(client);
        CompletableFuture<String> result = new CompletableFuture<>();
        provider.applyRequestMetadata(null, Runnable::run, new MetadataApplier() {
            @Override
            public void apply(Metadata headers) {
                try {
                    // Another request must complete while this callback is still executing.
                    result.complete(CompletableFuture.supplyAsync(() -> {
                                try {
                                    return authorization(provider);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .get(5, TimeUnit.SECONDS));
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
            }

            @Override
            public void fail(Status status) {
                result.completeExceptionally(status.asRuntimeException());
            }
        });
        assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo("Bearer shared");
        verify(client, times(1)).getAccessToken();
    }

    @Test
    void failedTokenFetchCanBeRetried() throws Exception {
        OAuthClient client = mock();
        TokenStatus token = mock();
        when(token.isValid()).thenReturn(true);
        when(token.getToken()).thenReturn("recovered");
        when(client.getAccessToken())
                .thenThrow(new IllegalStateException("unavailable"))
                .thenReturn(token);
        OAuthCredentialsProvider provider = new OAuthCredentialsProvider(client);
        CompletableFuture<Status> failure = new CompletableFuture<>();
        provider.applyRequestMetadata(null, Runnable::run, new MetadataApplier() {
            @Override
            public void apply(Metadata headers) {
                failure.completeExceptionally(new AssertionError("Token request should fail"));
            }

            @Override
            public void fail(Status status) {
                failure.complete(status);
            }
        });
        assertThat(failure.get(5, TimeUnit.SECONDS).getCode()).isEqualTo(Status.Code.UNAUTHENTICATED);
        assertThat(runConcurrently(16, () -> authorization(provider))).containsOnly("Bearer recovered");
        verify(client, times(2)).getAccessToken();
    }

    private static String authorization(OAuthCredentialsProvider provider) throws Exception {
        CompletableFuture<String> result = new CompletableFuture<>();
        provider.applyRequestMetadata(null, Runnable::run, new MetadataApplier() {
            @Override
            public void apply(Metadata headers) {
                result.complete(headers.get(AUTHORIZATION));
            }

            @Override
            public void fail(Status status) {
                result.completeExceptionally(status.asRuntimeException());
            }
        });
        return result.get(5, TimeUnit.SECONDS);
    }
}
