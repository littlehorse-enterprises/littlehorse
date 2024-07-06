package io.littlehorse.sdk.common.auth;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.littlehorse.sdk.common.proto.TenantId;
import java.util.Objects;
import java.util.concurrent.Executor;

public class TenantMetadataProvider extends CallCredentials {
    private static final Metadata.Key<String> tenantHeader =
            Metadata.Key.of("tenantId", Metadata.ASCII_STRING_MARSHALLER);
    private final String tenantId;

    public TenantMetadataProvider(TenantId tenantId) {
        Objects.requireNonNull(tenantId);
        this.tenantId = tenantId.getId();
    }

    public TenantMetadataProvider(String tenantId) {
        Objects.requireNonNull(tenantId);
        this.tenantId = tenantId;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        executor.execute(() -> {
            Metadata tenantMetadata = new Metadata();
            tenantMetadata.put(tenantHeader, tenantId);
            metadataApplier.apply(tenantMetadata);
        });
    }
}
