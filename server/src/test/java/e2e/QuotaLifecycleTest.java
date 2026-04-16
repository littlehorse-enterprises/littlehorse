package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.DeletePrincipalRequest;
import io.littlehorse.sdk.common.proto.DeleteQuotaRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.sdk.common.proto.PutPrincipalRequest;
import io.littlehorse.sdk.common.proto.PutQuotaRequest;
import io.littlehorse.sdk.common.proto.Quota;
import io.littlehorse.sdk.common.proto.QuotaId;
import io.littlehorse.sdk.common.proto.QuotaIdList;
import io.littlehorse.sdk.common.proto.SearchQuotaRequest;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
public class QuotaLifecycleTest {

    private LittleHorseBlockingStub client;
    private LHConfig config;

    private String tenantId() {
        return config.getTenantId().getId();
    }

    @Test
    void shouldPutAndGetTenantQuota() {
        Quota result = client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(50)
                .build());

        assertThat(result.getId().getTenant().getId()).isEqualTo(tenantId());
        assertThat(result.getId().hasPrincipal()).isFalse();
        assertThat(result.getWriteRequestsPerSecond()).isEqualTo(50);
        assertThat(result.hasCreatedAt()).isTrue();

        // Verify we can get it back (eventually consistent)
        QuotaId quotaId = QuotaId.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .build();
        Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
            Quota fetched = client.getQuota(quotaId);
            assertThat(fetched.getWriteRequestsPerSecond()).isEqualTo(50);
        });

        // Cleanup
        client.deleteQuota(DeleteQuotaRequest.newBuilder()
                .setId(quotaId)
                .build());
    }

    @Test
    void shouldPutAndGetPrincipalQuota() {
        String principalName = "test-principal-" + UUID.randomUUID();
        PrincipalId principalId =
                PrincipalId.newBuilder().setId(principalName).build();

        // The principal must exist before we can create a quota for it
        client.putPrincipal(PutPrincipalRequest.newBuilder()
                .setId(principalName)
                .setOverwrite(true)
                .build());

        Quota result = client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setPrincipal(principalId)
                .setWriteRequestsPerSecond(25)
                .build());

        assertThat(result.getId().getTenant().getId()).isEqualTo(tenantId());
        assertThat(result.getId().getPrincipal().getId()).isEqualTo(principalName);
        assertThat(result.getWriteRequestsPerSecond()).isEqualTo(25);

        // Verify we can get it back (eventually consistent)
        Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
            Quota fetched = client.getQuota(QuotaId.newBuilder()
                    .setTenant(TenantId.newBuilder().setId(tenantId()))
                    .setPrincipal(principalId)
                    .build());
            assertThat(fetched.getWriteRequestsPerSecond()).isEqualTo(25);
        });

        // Cleanup
        client.deleteQuota(DeleteQuotaRequest.newBuilder()
                .setId(QuotaId.newBuilder()
                        .setTenant(TenantId.newBuilder().setId(tenantId()))
                        .setPrincipal(principalId))
                .build());
        client.deletePrincipal(DeletePrincipalRequest.newBuilder()
                .setId(principalId)
                .build());
    }

    @Test
    void shouldBeIdempotent() {
        PutQuotaRequest request = PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(100)
                .build();

        Quota first = client.putQuota(request);
        Quota second = client.putQuota(request);

        assertThat(first.getWriteRequestsPerSecond()).isEqualTo(second.getWriteRequestsPerSecond());
        assertThat(first.getId()).isEqualTo(second.getId());

        // Cleanup
        client.deleteQuota(DeleteQuotaRequest.newBuilder()
                .setId(first.getId())
                .build());
    }

    @Test
    void shouldUpdateExistingQuota() {
        Quota original = client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(50)
                .build());

        assertThat(original.getWriteRequestsPerSecond()).isEqualTo(50);

        Quota updated = client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(200)
                .build());

        assertThat(updated.getWriteRequestsPerSecond()).isEqualTo(200);

        // Verify the update persisted (eventually consistent)
        Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
            Quota fetched = client.getQuota(original.getId());
            assertThat(fetched.getWriteRequestsPerSecond()).isEqualTo(200);
        });

        // Cleanup
        client.deleteQuota(DeleteQuotaRequest.newBuilder()
                .setId(original.getId())
                .build());
    }

    @Test
    void shouldDeleteQuota() {
        QuotaId quotaId = client.putQuota(PutQuotaRequest.newBuilder()
                        .setTenant(TenantId.newBuilder().setId(tenantId()))
                        .setWriteRequestsPerSecond(50)
                        .build())
                .getId();

        // Wait for the quota to be visible on all servers before deleting
        Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
            Quota q = client.getQuota(quotaId);
            assertThat(q.getWriteRequestsPerSecond()).isEqualTo(50);
        });

        client.deleteQuota(
                DeleteQuotaRequest.newBuilder().setId(quotaId).build());

        // Verify it is gone (eventually consistent)
        Awaitility.await().atMost(Duration.ofSeconds(4)).until(() -> {
            try {
                client.getQuota(quotaId);
                return false;
            } catch (Exception exn) {
                return LHTestExceptionUtil.isNotFoundException(exn);
            }
        });
    }

    @Test
    void shouldSearchQuotas() {
        String principalName = "search-principal-" + UUID.randomUUID();
        PrincipalId principalId =
                PrincipalId.newBuilder().setId(principalName).build();

        // Create the principal first
        client.putPrincipal(PutPrincipalRequest.newBuilder()
                .setId(principalName)
                .setOverwrite(true)
                .build());

        // Create a tenant-level quota
        Quota tenantQuota = client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(100)
                .build());

        // Create a principal-level quota
        Quota principalQuota = client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setPrincipal(principalId)
                .setWriteRequestsPerSecond(50)
                .build());

        // Search by tenant
        Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
            QuotaIdList results = client.searchQuota(SearchQuotaRequest.newBuilder()
                    .setTenantId(tenantId())
                    .build());

            assertThat(results.getResultsList()).hasSizeGreaterThanOrEqualTo(2);
        });

        // Cleanup
        client.deleteQuota(
                DeleteQuotaRequest.newBuilder().setId(tenantQuota.getId()).build());
        client.deleteQuota(DeleteQuotaRequest.newBuilder()
                .setId(principalQuota.getId())
                .build());
        client.deletePrincipal(DeletePrincipalRequest.newBuilder()
                .setId(principalId)
                .build());
    }

    @Test
    void shouldFailGetOnNonexistentQuota() {
        QuotaId nonExistent = QuotaId.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setPrincipal(
                        PrincipalId.newBuilder().setId("nonexistent-" + UUID.randomUUID()))
                .build();

        assertThatThrownBy(() -> client.getQuota(nonExistent))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(exn -> assertThat(LHTestExceptionUtil.isNotFoundException(exn))
                        .isTrue());
    }
}
