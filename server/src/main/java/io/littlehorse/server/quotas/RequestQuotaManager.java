package io.littlehorse.server.quotas;

import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.rpc.Code;
import com.google.rpc.RetryInfo;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.littlehorse.common.model.getable.global.acl.QuotaModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestQuotaManager {

    private static final long WINDOW_MS = 500L;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final BackendInternalComms internalComms;
    private final Map<String, QuotaState> quotaStates = new ConcurrentHashMap<>();

    public RequestQuotaManager(BackendInternalComms internalComms) {
        this.internalComms = internalComms;
    }

    public void enforceOrThrow(RequestExecutionContext context) {
        List<StateHandle> handles = loadApplicableQuotas(context);
        if (handles.isEmpty()) {
            return;
        }

        handles.sort(Comparator.comparing(StateHandle::key));
        long nowNanos = System.nanoTime();
        int serverCount = Math.max(1, internalComms.getAllInternalHosts().size());

        withLocks(handles, 0, () -> {
            long retryDelayMs = 0L;
            for (StateHandle handle : handles) {
                retryDelayMs = Math.max(
                        retryDelayMs,
                        handle.state.previewRetryDelayMillis(
                                handle.quota.getWriteRequestsPerSecond(), serverCount, nowNanos));
            }

            if (retryDelayMs > 0) {
                throw quotaExceeded(retryDelayMs);
            }

            for (StateHandle handle : handles) {
                handle.state.recordAccepted(handle.quota.getWriteRequestsPerSecond(), serverCount, nowNanos);
            }
        });
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
        return new StateHandle(key, quota, quotaStates.computeIfAbsent(key, ignored -> new QuotaState()));
    }

    private void withLocks(List<StateHandle> handles, int index, Runnable operation) {
        if (index >= handles.size()) {
            operation.run();
            return;
        }

        synchronized (handles.get(index).state) {
            withLocks(handles, index + 1, operation);
        }
    }

    private StatusRuntimeException quotaExceeded(long retryDelayMs) {
        RetryInfo retryInfo = RetryInfo.newBuilder()
                .setRetryDelay(Duration.newBuilder()
                        .setSeconds(retryDelayMs / 1_000)
                        .setNanos((int) ((retryDelayMs % 1_000) * 1_000_000))
                        .build())
                .build();

        com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
                .setCode(Code.RESOURCE_EXHAUSTED.getNumber())
                .setMessage("Quota exceeded. Retry after %dms.".formatted(retryDelayMs))
                .addDetails(Any.pack(retryInfo))
                .build();

        return StatusProto.toStatusRuntimeException(status);
    }

    private static final class StateHandle {
        private final String key;
        private final QuotaModel quota;
        private final QuotaState state;

        private StateHandle(String key, QuotaModel quota, QuotaState state) {
            this.key = key;
            this.quota = quota;
            this.state = state;
        }

        private String key() {
            return key;
        }
    }

    private static final class QuotaState {
        private double availablePermits;
        private long lastRefillNanos;
        private boolean initialized;

        private long previewRetryDelayMillis(int writeRequestsPerSecond, int serverCount, long nowNanos) {
            double ratePerSecond = Math.max(0.0, writeRequestsPerSecond / (double) serverCount);
            refresh(ratePerSecond, nowNanos);
            if (availablePermits >= 1.0) {
                return 0L;
            }
            if (ratePerSecond <= 0.0) {
                return WINDOW_MS;
            }

            double missingPermits = 1.0 - availablePermits;
            long delayMs = (long) Math.ceil((missingPermits / ratePerSecond) * 1_000.0);
            long roundedDelayMs = ((Math.max(delayMs, 1L) + WINDOW_MS - 1) / WINDOW_MS) * WINDOW_MS;
            return Math.max(WINDOW_MS, roundedDelayMs);
        }

        private void recordAccepted(int writeRequestsPerSecond, int serverCount, long nowNanos) {
            double ratePerSecond = Math.max(0.0, writeRequestsPerSecond / (double) serverCount);
            refresh(ratePerSecond, nowNanos);
            availablePermits -= 1.0;
        }

        private void refresh(double ratePerSecond, long nowNanos) {
            double capacity = Math.max(1.0, ratePerSecond * WINDOW_MS / 1_000.0);
            if (!initialized) {
                availablePermits = capacity;
                lastRefillNanos = nowNanos;
                initialized = true;
                return;
            }

            double elapsedSeconds = (nowNanos - lastRefillNanos) / (double) NANOS_PER_SECOND;
            if (elapsedSeconds > 0) {
                availablePermits = Math.min(capacity, availablePermits + (elapsedSeconds * ratePerSecond));
                lastRefillNanos = nowNanos;
            }
        }
    }
}
