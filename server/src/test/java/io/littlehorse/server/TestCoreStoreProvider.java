package io.littlehorse.server;

import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class TestCoreStoreProvider extends CoreStoreProvider {

    private final TestRequestExecutionContext requestContext;

    public TestCoreStoreProvider(TestRequestExecutionContext requestContext) {
        super(null);
        this.requestContext = requestContext;
    }

    @Override
    public ReadOnlyKeyValueStore<String, Bytes> nativeCoreStore() {
        return requestContext.getCoreNativeStore();
    }

    @Override
    public ReadOnlyKeyValueStore<String, Bytes> getNativeGlobalStore() {
        return requestContext.getGlobalMetadataNativeStore();
    }
}
