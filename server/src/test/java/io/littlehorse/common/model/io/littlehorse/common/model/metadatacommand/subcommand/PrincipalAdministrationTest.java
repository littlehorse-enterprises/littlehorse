package io.littlehorse.common.model.io.littlehorse.common.model.metadatacommand.subcommand;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutPrincipalRequestModel;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.PutPrincipalRequest;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PrincipalAdministrationTest {

    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    private final MetadataCache metadataCache = new MetadataCache();
    private final KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();
    private final MockProcessorContext<String, Bytes> mockProcessorContext = new MockProcessorContext<>();

    private MetadataProcessor metadataProcessor;

    private PutPrincipalRequestModel putPrincipalRequest =
            PutPrincipalRequestModel.fromProto(principalRequestToProcess(), PutPrincipalRequestModel.class);

    private final String tenantId = "test-tenant-id";
    private final String principalId = "test-principal-id";

    private final LHStore defaultStore = LHStore.defaultStore(nativeMetadataStore);

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
    }

    @Test
    public void supportStorePrincipal() {
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        command.setTenantId(tenantId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L));
        StoredGetable<Principal, PrincipalModel> storedPrincipal = defaultStore.get(new PrincipalIdModel(principalId));
        assertThat(storedPrincipal).isNotNull();
        assertThat(storedPrincipal.getStoredObject().getTenantIds()).containsExactly(tenantId);
        assertThat(storedPrincipal.getStoredObject().getDefaultTenantId()).isEqualTo(tenantId);
    }

    private PutPrincipalRequest principalRequestToProcess() {
        return PutPrincipalRequest.newBuilder().setId(principalId).build();
    }
}
