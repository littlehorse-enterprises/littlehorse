package io.littlehorse.server.streams.topology.core.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.UUID;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MetadataGlobalStoreProcessorTest {

    private final MetadataCache metadataCache = new MetadataCache();

    private final MetadataGlobalStoreProcessor metadataProcessor = new MetadataGlobalStoreProcessor(metadataCache);
    private final ProcessorContext<Void, Void> mockCtx = mock();
    private final KeyValueStore<String, Bytes> mockStore = mock();
    private String tenantId = "default";
    private WfSpecIdModel wfSpecId = new WfSpecIdModel("my-wf-spec", 2, 5);

    @BeforeEach
    public void setup() {
        when(mockCtx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE)).thenReturn(mockStore);
        metadataProcessor.init(mockCtx);
    }

    @Test
    public void shouldStoreMetadataObject() {
        StoredGetable wfSpec = new StoredGetable<>(TestUtil.wfSpec("my-wf-spec"));
        String fullKey = tenantId + "/" + wfSpec.getFullStoreKey();
        Bytes valueBytes = Bytes.wrap(wfSpec.toBytes());
        metadataProcessor.process(new Record<>(fullKey, valueBytes, 0L));
        assertThat(metadataCache.get(fullKey)).isNotNull();
        verify(mockStore).put(eq(fullKey), any());
    }

    @Test
    public void shouldStoreClusterMetadataObject() {
        StoredGetable tenant = new StoredGetable<>(new TenantModel(new TenantIdModel("my-tenant")));
        String fullKey = tenant.getFullStoreKey();
        Bytes valueBytes = Bytes.wrap(tenant.toBytes());
        metadataProcessor.process(new Record<>(fullKey, valueBytes, 0L));
        assertThat(metadataCache.get(fullKey)).isNotNull();
        verify(mockStore).put(eq(fullKey), any());
    }

    @Test
    public void shouldDeleteMetadataObject() {
        String fullKey = "";
        // store a value
        WfSpecModel wfSpec = TestUtil.wfSpec("my-wf-spec");
        Bytes valueBytes = Bytes.wrap(wfSpec.toBytes());
        metadataProcessor.process(new Record<>(fullKey, valueBytes, 0L));
        // delete the value
        metadataProcessor.process(new Record<>(fullKey, null, 0L));
        assertThat(metadataCache.get(fullKey)).isNull();
        verify(mockStore).delete(fullKey);
    }

    @Test
    public void shouldIgnoreNonCacheableObject() {
        WfRunModel wfRun = TestUtil.wfRun(UUID.randomUUID().toString());
        String fullKey = tenantId + "/" + wfRun.getObjectId().getStoreableKey();
        Bytes valueBytes = Bytes.wrap(wfRun.toBytes());
        metadataProcessor.process(new Record<>(fullKey, valueBytes, 0L));
        assertThat(metadataCache.get(fullKey)).isNull();
        verify(mockStore, atMostOnce()).put(eq(fullKey), any());
    }
}
