package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.test.LHTest;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@LHTest
public class TenantAdministrationTest {

    private LittleHorseBlockingStub client;

    @ParameterizedTest
    @EnumSource(
            value = OutputTopicConfig.OutputTopicRecordingLevel.class,
            names = {"ALL_ENTITY_EVENTS", "NO_ENTITY_EVENTS"})
    void shouldFetchPreserveReplaceAndDisableOutputSettings(OutputTopicConfig.OutputTopicRecordingLevel level) {
        String id = "tenant-" + UUID.randomUUID();
        OutputTopicConfig output =
                OutputTopicConfig.newBuilder().setDefaultRecordingLevel(level).build();
        Tenant original = client.putTenant(PutTenantRequest.newBuilder()
                .setId(id)
                .setName("Original")
                .setOutputTopicConfig(output)
                .build());
        awaitTenant(original);
        Tenant fetched = client.getTenant(TenantId.newBuilder().setId(id).build());
        Tenant renamed = client.putTenant(PutTenantRequest.newBuilder()
                .setId(id)
                .setName("Renamed")
                .setOutputTopicConfig(fetched.getOutputTopicConfig())
                .build());
        assertThat(renamed.getName()).isEqualTo("Renamed");
        assertThat(renamed.hasOutputTopicConfig()).isTrue();
        assertThat(renamed.getOutputTopicConfig()).isEqualTo(output);
        Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> assertThat(
                        client.getTenant(TenantId.newBuilder().setId(id).build()))
                .isEqualTo(renamed));
        OutputTopicConfig replacement = OutputTopicConfig.newBuilder()
                .setDefaultRecordingLevel(
                        level == OutputTopicConfig.OutputTopicRecordingLevel.ALL_ENTITY_EVENTS
                                ? OutputTopicConfig.OutputTopicRecordingLevel.NO_ENTITY_EVENTS
                                : OutputTopicConfig.OutputTopicRecordingLevel.ALL_ENTITY_EVENTS)
                .build();
        Tenant replaced = client.putTenant(PutTenantRequest.newBuilder()
                .setId(id)
                .setOutputTopicConfig(replacement)
                .build());
        assertThat(replaced.getOutputTopicConfig()).isEqualTo(replacement);
        assertThat(replaced.getName()).isEqualTo("Renamed");
        Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> assertThat(
                        client.getTenant(TenantId.newBuilder().setId(id).build()))
                .isEqualTo(replaced));
        Tenant disabled = client.putTenant(
                PutTenantRequest.newBuilder().setId(id).setName("Disabled").build());
        assertThat(disabled.hasOutputTopicConfig()).isFalse();
        assertThat(disabled.getName()).isEqualTo("Disabled");
        awaitTenant(disabled);
        Tenant stillDisabled =
                client.putTenant(PutTenantRequest.newBuilder().setId(id).build());
        assertThat(stillDisabled.hasOutputTopicConfig()).isFalse();
        assertThat(stillDisabled.getName()).isEqualTo("Disabled");
        awaitTenant(stillDisabled);
        Tenant enabled = client.putTenant(PutTenantRequest.newBuilder()
                .setId(id)
                .setOutputTopicConfig(output)
                .build());
        assertThat(enabled.getOutputTopicConfig()).isEqualTo(output);
        assertThat(enabled.hasOutputTopicConfig()).isTrue();
        awaitTenant(enabled);
        Tenant cleared =
                client.putTenant(PutTenantRequest.newBuilder().setId(id).build());
        assertThat(cleared.hasOutputTopicConfig()).isFalse();
        assertThat(cleared.getName()).isEqualTo("Disabled");
        awaitTenant(cleared);
    }

    private void awaitTenant(Tenant expected) {
        Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> assertThat(
                        client.getTenant(expected.getId()))
                .isEqualTo(expected));
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
