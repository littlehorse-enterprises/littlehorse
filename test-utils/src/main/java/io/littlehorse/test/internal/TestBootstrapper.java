package io.littlehorse.test.internal;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.server.auth.ServerAuthorizer;
import java.util.concurrent.Executor;

public interface TestBootstrapper {
    LHConfig getWorkerConfig();

    LittleHorseBlockingStub getLhClient();

    final class MockCallCredentials extends CallCredentials {

        private final TenantIdModel tenantId;

        MockCallCredentials(final TenantIdModel tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
            executor.execute(() -> {
                try {
                    Metadata headers = new Metadata();
                    if (tenantId != null && tenantId.getId() != null) {
                        headers.put(ServerAuthorizer.TENANT_ID, tenantId.getId());
                        metadataApplier.apply(headers);
                    }
                } catch (Exception e) {
                    metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
                }
            });
        }
    }
}
