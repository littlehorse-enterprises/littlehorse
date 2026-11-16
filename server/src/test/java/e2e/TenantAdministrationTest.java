package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.test.LHTest;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@LHTest
public class TenantAdministrationTest {

    private LittleHorseBlockingStub client;

    @Test
    void shouldKeepOutputTopicConfigWhenRenamedWithSameConfig() {
        String id = "tenant-" + UUID.randomUUID();
        OutputTopicConfig output = OutputTopicConfig.newBuilder()
                .setDefaultRecordingLevel(OutputTopicConfig.OutputTopicRecordingLevel.ALL_ENTITY_EVENTS)
                .build();
        Tenant original = client.putTenant(PutTenantRequest.newBuilder()
                .setId(id)
                .setName("Original")
                .setOutputTopicConfig(output)
                .build());

        Tenant renamed = client.putTenant(PutTenantRequest.newBuilder()
                .setId(id)
                .setName("Renamed")
                .setOutputTopicConfig(original.getOutputTopicConfig())
                .build());

        assertThat(renamed.getName()).isEqualTo("Renamed");
        assertThat(renamed.hasOutputTopicConfig()).isTrue();
        assertThat(renamed.getOutputTopicConfig()).isEqualTo(output);
        Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> assertThat(
                        client.getTenant(renamed.getId()))
                .isEqualTo(renamed));
    }

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
