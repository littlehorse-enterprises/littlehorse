package io.littlehorse.common.util;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.CompactionStyle;
import org.rocksdb.Options;

@Slf4j
public class RocksConfigSetter implements RocksDBConfigSetter {

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
        log.trace("Overriding rocksdb settings for store {}", storeName);

        // This is useful for getting the LOG file in the Kafka Streams
        // State Dir. For example, the folks at Speedb often request that
        // for performance tuning. So leave this comment here in case
        // it's necessary to get some additional perf debugging.
        //
        // options.setInfoLogLevel(InfoLogLevel.DEBUG_LEVEL);

        // Level compaction has less space amplification than Universal, meaning
        // that the disk usage balloons less. Additionally, I've noticed that it
        // is slightly less prone to write stalls.
        options.setCompactionStyle(CompactionStyle.LEVEL);

        // TODO: Make this configurable.
        // This is set to 32MB, whereas default is 16MB. Increasing write buffer
        // size has a *drastic* positive benefit on performance.
        options.setWriteBufferSize(32 * 1024 * 1024L);

        // Streams default is 3
        options.setMaxWriteBufferNumber(5);
    }

    @Override
    public void close(final String storeName, final Options options) {}
}
