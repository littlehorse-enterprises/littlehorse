package io.littlehorse.server.auth.internalport;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.server.auth.RequestAuthorizer;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.concurrent.Executor;

/**
 * Call Credentials that allows the LH Server to propagate information about the calling Tenant
 * and Principal when making an Interactive Query between two different LH Servers on the
 * internal port.
 */
public class InternalCallCredentials extends CallCredentials {
    private final AuthorizationContext currentAuthorization;

    private InternalCallCredentials(AuthorizationContext authorization) {
        this.currentAuthorization = authorization;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier metadataApplier) {
        appExecutor.execute(() -> {
            Metadata metadataToPropagate = new Metadata();
            metadataToPropagate.put(
                    RequestAuthorizer.TENANT_ID, currentAuthorization.tenantId().toString());
            metadataToPropagate.put(
                    RequestAuthorizer.CLIENT_ID,
                    currentAuthorization.principalId().toString());
            try {
                metadataApplier.apply(metadataToPropagate);
            } catch (Exception ex) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(ex));
            }
        });
    }

    public static InternalCallCredentials forContext(BackgroundContext callbackContext) {
        return new InternalCallCredentials(callbackContext.authorization());
    }

    public static InternalCallCredentials forContext(RequestExecutionContext requestContext) {
        return new InternalCallCredentials(requestContext.authorization());
    }

    public static InternalCallCredentials forContext(CoreProcessorContext processorContext) {
        return new InternalCallCredentials(processorContext.authorization());
    }
}
