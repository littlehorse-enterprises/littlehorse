package e2e;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.test.LHTest;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@LHTest
public class TenantAdministrationTest {

    private LittleHorseBlockingStub client;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/Acme", "Acme/Production", "Acme/"})
    void shouldRejectSlashInTenantName(String name) {
        PutTenantRequest request = PutTenantRequest.newBuilder()
                .setId("tenant-" + UUID.randomUUID())
                .setName(name)
                .build();

        assertThatThrownBy(() -> client.putTenant(request))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("INVALID_ARGUMENT: Tenant name must not contain '/'");
    }
}
