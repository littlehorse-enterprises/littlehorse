package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.rocksdb.Cache;
import org.rocksdb.CompactionStyle;
import org.rocksdb.Options;

@Slf4j
public class RocksConfigSetter implements RocksDBConfigSetter {

    // Need to inject the LHServerConfig, but Kafka Streams requires we pass in a Class with
    // a default constructor. So the only way to do that is to have a static singleton which
    // we set elsewhere in the code.
    public static LHServerConfig serverConfig;

    // From RocksDB docs: default is 4kb, but Facebook tends to use 16-32kb in production.
    // With longer keys (we have a lot of long keys), higher block size is recommended. We
    // increase the block size to 32kb here.
    //
    // The danger here is that we don't have a "hard limit" on memory allocated by these
    // blocks. To do that, we would need to investigate creating an LRUCache with a
    // special index filter block ratio, and then do some math. For more context:
    //
    // NOTE: Increasing the Block Size makes the un-managed memory footprint SMALLER because
    // there are fewer blocks to index. However, I think it makes reads a bit slower.
    //
    // Confluent: https://docs.confluent.io/platform/current/streams/developer-guide/memory-mgmt.html
    // Rocksdb: https://github.com/facebook/rocksdb/wiki/Memory-usage-in-RocksDB#indexes-and-filter-blocks
    private static final long BLOCK_SIZE = 1024 * 32;

    // From RocksDB docs: this allows better performance when using jemalloc
    // https://github.com/facebook/rocksdb/wiki/Memory-usage-in-RocksDB#indexes-and-filter-blocks
    private static final boolean OPTIMIZE_FILTERS_FOR_MEMORY = true;

    // Most of the time, when we do a get() we end up finding an object. This config makes RocksDB
    // NOT put a Bloom Filter on the last level of SST files. This reduces memory usage of the
    // Bloom Filter by 90%, but it costs one IO operation on every get() for a missing key.
    // In my opinion, it's a good trade-off.
    private static final boolean OPTIMIZE_FILTERS_FOR_HITS = true;

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
        log.trace("Overriding rocksdb settings for store {}", storeName);

        BlockBasedTableConfigWithAccessibleCache tableConfig =
                (BlockBasedTableConfigWithAccessibleCache) options.tableFormatConfig();
        if (serverConfig.getGlobalRocksdbBlockCache() != null) {
            // Streams provisions a *NON-shared* 50MB cache for every RocksDB instance. Need
            // to .close() it to avoid leaks so that we can provide a global one.
            Cache oldCache = tableConfig.blockCache();
            tableConfig.setBlockCache(serverConfig.getGlobalRocksdbBlockCache());
            oldCache.close();
        }

        tableConfig.setOptimizeFiltersForMemory(OPTIMIZE_FILTERS_FOR_MEMORY);
        tableConfig.setBlockSize(BLOCK_SIZE);
        options.setTableFormatConfig(tableConfig);

        options.setOptimizeFiltersForHits(OPTIMIZE_FILTERS_FOR_HITS);
        options.setCompactionStyle(CompactionStyle.LEVEL);

        // Memtable size
        options.setWriteBufferSize(
                isCoreStore(storeName) ? serverConfig.getCoreMemtableSize() : serverConfig.getTimerMemtableSize());

        if (serverConfig.getGlobalRocksdbWriteBufferManager() != null) {
            options.setWriteBufferManager(serverConfig.getGlobalRocksdbWriteBufferManager());
        }
        // Streams default is 3
        options.setMaxWriteBufferNumber(5);

        // Future Work: Enable larger scaling by using Partitioned Index Filters
        // https://github.com/facebook/rocksdb/wiki/Partitioned-Index-Filters
        //
        // We should do scale testing with the LH Canary to determine whether that should be enabled
        // by default or as a configuration.
    }

    @Override
    public void close(final String storeName, final Options options) {}

    private boolean isCoreStore(String storeName) {
        return !storeName.contains("timer");
    }
}
