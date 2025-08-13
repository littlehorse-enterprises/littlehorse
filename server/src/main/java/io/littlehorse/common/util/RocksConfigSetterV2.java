package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.CompactionStyle;
import org.rocksdb.Options;

@Slf4j
public class RocksConfigSetterV2 implements RocksDBConfigSetter {

    // This key is used to inject the LHServerConfig into the Map<String, Object> configs
    // passed into the setConfig() method.
    public static final String LH_SERVER_CONFIG_KEY = "obiwan.kenobi";

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
        log.info("Overriding rocksdb v2 settings for store {}", storeName);

        LHServerConfig serverConfig = (LHServerConfig) configs.get(LH_SERVER_CONFIG_KEY);

        options.setUseDirectIoForFlushAndCompaction(serverConfig.useDirectIOForRocksDB());
        options.setUseDirectReads(serverConfig.useDirectIOForRocksDB());
        options.setCompactionStyle(CompactionStyle.UNIVERSAL);
    }

    @Override
    public void close(final String storeName, final Options options) {}
}
