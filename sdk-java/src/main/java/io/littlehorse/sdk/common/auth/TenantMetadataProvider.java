package io.littlehorse.sdk.common.auth;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.littlehorse.sdk.common.proto.TenantId;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * gRPC call credentials provider that injects the tenant id header.
 */
public class TenantMetadataProvider extends CallCredentials {
    private static final Metadata.Key<String> tenantHeader =
            Metadata.Key.of("tenantId", Metadata.ASCII_STRING_MARSHALLER);
    private final String tenantId;

    /**
     * Creates provider from a TenantId protobuf object.
     *
     * @param tenantId tenant id wrapper
     */
    public TenantMetadataProvider(TenantId tenantId) {
        Objects.requireNonNull(tenantId);
        this.tenantId = tenantId.getId();
    }

    /**
     * Creates provider from a raw tenant id string.
     *
     * @param tenantId tenant id value
     */
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
