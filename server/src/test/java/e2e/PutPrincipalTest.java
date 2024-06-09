package e2e;

import com.google.protobuf.Timestamp;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.test.LHTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class PutPrincipalTest {

    LittleHorseBlockingStub client;

    @Test
    void putTenantShouldBeIdempotent() {
        String tenantId = "obi-wan-asdfqetgasjd";
        PutTenantRequest req = PutTenantRequest.newBuilder().setId(tenantId).build();

        Tenant original = client.putTenant(req);
        Timestamp createdAt = original.getCreatedAt();

        Tenant duplicated = client.putTenant(req);
        Timestamp createdAtAgain = duplicated.getCreatedAt();

        Assertions.assertThat(createdAt).isEqualTo(createdAtAgain);

        Tenant fetchedYetAgain =
                client.getTenant(TenantId.newBuilder().setId(tenantId).build());
        Timestamp fetchedTimestamp = fetchedYetAgain.getCreatedAt();

        Assertions.assertThat(fetchedTimestamp).isEqualTo(createdAt);
    }
}
