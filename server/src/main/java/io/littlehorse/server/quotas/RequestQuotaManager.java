package io.littlehorse.server.quotas;

import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.rpc.Code;
import com.google.rpc.RetryInfo;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;
import io.littlehorse.common.model.getable.global.acl.QuotaModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestQuotaManager {

    private final BackendInternalComms internalComms;
    private final Map<String, QuotaState> quotaStates;

    public RequestQuotaManager(BackendInternalComms internalComms) {
        this.internalComms = internalComms;
        this.quotaStates = new ConcurrentHashMap<>();
    }

    public void enforceOrThrow(RequestExecutionContext context) {
        // As of now, handles can only return 0, 1, or 2 quotas (tenant, and/or tenant+principal)
        List<StateHandle> handles = loadApplicableQuotas(context);
        if (handles.isEmpty()) {
            return;
        }

        long nowNanos = System.nanoTime();
        int serverCount = Math.max(1, internalComms.getAllInternalHosts().size());

        synchronized (this) {
            long retryDelayMs = 0L;
            for (StateHandle handle : handles) {
                retryDelayMs = Math.max(
                        retryDelayMs,
                        handle.getState()
                                .previewRetryDelayMillis(
                                        handle.getQuota().getWriteRequestsPerSecond(), serverCount, nowNanos));
            }

            if (retryDelayMs > 0) {
                Status status = Status.newBuilder()
                        .setCode(Code.RESOURCE_EXHAUSTED.getNumber())
                        .setMessage("Quota exceeded. Retry after %dms.".formatted(retryDelayMs))
                        .addDetails(Any.pack(RetryInfo.newBuilder()
                                .setRetryDelay(Duration.newBuilder()
                                        .setSeconds(retryDelayMs / 1_000)
                                        .setNanos((int) ((retryDelayMs % 1_000) * 1_000_000))
                                        .build())
                                .build()))
                        .build();

                throw StatusProto.toStatusRuntimeException(status);
            }

            for (StateHandle handle : handles) {
                handle.getState().recordAccepted(handle.getQuota().getWriteRequestsPerSecond(), serverCount, nowNanos);
            }
        }
    }

    private List<StateHandle> loadApplicableQuotas(RequestExecutionContext context) {
        TenantIdModel tenantId = context.authorization().tenantId();
        PrincipalIdModel principalId = context.authorization().principalId();

        List<StateHandle> out = new ArrayList<>();

        QuotaIdModel tenantQuotaId = new QuotaIdModel(tenantId);
        QuotaModel tenantQuota = context.metadataManager().get(tenantQuotaId);
        if (tenantQuota != null) {
            out.add(stateHandle(tenantQuota));
        }

        QuotaIdModel principalQuotaId = new QuotaIdModel(tenantId, principalId);
        QuotaModel principalQuota = context.metadataManager().get(principalQuotaId);
        if (principalQuota != null) {
            out.add(stateHandle(principalQuota));
        }

        return out;
    }

    private StateHandle stateHandle(QuotaModel quota) {
        String key = quota.getObjectId().toString();
        return new StateHandle(quota, quotaStates.computeIfAbsent(key, ignored -> new QuotaState()));
    }
}
