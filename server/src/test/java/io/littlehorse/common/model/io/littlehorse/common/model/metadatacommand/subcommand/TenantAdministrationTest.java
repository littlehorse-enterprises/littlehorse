package io.littlehorse.common.model.io.littlehorse.common.model.metadatacommand.subcommand;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.common.proto.PutTenantRequest;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.MetadataProcessorDAOImpl;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
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
public class TenantAdministrationTest {

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

    private final String tenantId = "test-tenant-id";

    private final PutTenantRequestModel putTenantRequest =
            PutTenantRequestModel.fromProto(putTenantRequest(), PutTenantRequestModel.class, mock());

    private MetadataProcessorDAO metadataDao;

    private Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, LHConstants.ANONYMOUS_PRINCIPAL);

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
        metadataDao = new MetadataProcessorDAOImpl(ModelStore.defaultStore(nativeMetadataStore), metadataCache, null);
    }

    @Test
    public void supportStoringNewTenant() {
        MetadataCommandModel command = new MetadataCommandModel(putTenantRequest);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(UUID.randomUUID().toString(), command, 0L, metadata));
        assertThat(storedTenant()).isNotNull();
        assertThat(storedTenant().getObjectId()).isNotNull();
    }

    @Test
    public void shouldValidateExistingTenant() {
        MetadataCommandModel command = new MetadataCommandModel(putTenantRequest);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(UUID.randomUUID().toString(), command, 0L, metadata));
        metadataProcessor.process(new Record<>(UUID.randomUUID().toString(), command, 0L, metadata));
        verify(server, times(1)).sendErrorToClient(any(), any());
        assertThat(storedTenant()).isNotNull();
    }

    private TenantModel storedTenant() {
        return metadataDao.get(new TenantIdModel(tenantId));
    }

    private PutTenantRequest putTenantRequest() {
        return PutTenantRequest.newBuilder().setId(tenantId).build();
    }
}
