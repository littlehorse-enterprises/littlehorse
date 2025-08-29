package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.IndexType;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.Options;
import org.rocksdb.RateLimiter;

@Slf4j
public class RocksConfigSetter implements RocksDBConfigSetter {

    // This key is used to inject the LHServerConfig into the Map<String, Object> configs
    // passed into the setConfig() method.
    public static final String LH_SERVER_CONFIG_KEY = "obiwan.kenobi";

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

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
        log.trace("Overriding rocksdb settings for store {}", storeName);

        LHServerConfig serverConfig = (LHServerConfig) configs.get(LH_SERVER_CONFIG_KEY);

        BlockBasedTableConfigWithAccessibleCache tableConfig =
                (BlockBasedTableConfigWithAccessibleCache) options.tableFormatConfig();
        if (serverConfig.getGlobalRocksdbBlockCache() != null) {
            // Streams provisions a *NON-shared* 50MB cache for every RocksDB instance. Need
            // to .close() it to avoid leaks so that we can provide a global one.
            Cache oldCache = tableConfig.blockCache();
            tableConfig.setBlockCache(serverConfig.getGlobalRocksdbBlockCache());
            oldCache.close();
        }

        // Bloom Filters: Partitioned Index Filters, cached.
        tableConfig.setFilterPolicy(new BloomFilter(10)); // 10 bits per key is default.
        tableConfig.setPartitionFilters(true);
        tableConfig.setIndexType(IndexType.kTwoLevelIndexSearch);
        tableConfig.setOptimizeFiltersForMemory(true);
        tableConfig.setBlockSize(BLOCK_SIZE);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        options.setOptimizeFiltersForHits(false);

        options.setUseDirectIoForFlushAndCompaction(serverConfig.useDirectIOForRocksDB());
        options.setUseDirectReads(serverConfig.useDirectIOForRocksDB());

        if (serverConfig.getRocksDBUseLevelCompaction()) {
            // Level compaction has higher write amplification and lower read amplification.
            // Therefore, we use other configs to attempt to reduce WA.
            options.setCompactionStyle(CompactionStyle.LEVEL);

            // Configure gradual slowdowns of writes
            options.setLevel0FileNumCompactionTrigger(10); // Default 4. Larger compactions -> less WA.
            options.setLevel0SlowdownWritesTrigger(20); // Default 20.
            options.setLevel0StopWritesTrigger(48); // Default 36. Higher RA, reduces stop-the-world.

            // Configure how levels grow.
            options.setTargetFileSizeBase(64 * 1024L * 1024L); // 64MB, default.
            options.setMaxBytesForLevelBase(1024L * 512L); // 512MB in L1
            options.setMaxBytesForLevelMultiplier(10); // default: 5GB L2, 50GB L3, etc

            tableConfig.setPinL0FilterAndIndexBlocksInCache(true);

            // Use blobs: larger values are stored separately from the keys.
            // This reduces write amplification during compaction at the cost of slower
            // reads.
            options.setEnableBlobFiles(true);
            options.setBlobFileSize(512L);
            options.setCompressionType(CompressionType.SNAPPY_COMPRESSION);
        } else {
            options.setCompactionStyle(CompactionStyle.UNIVERSAL);
        }

        options.setTableFormatConfig(tableConfig);

        Optional<InfoLogLevel> rocksDBLogLevel = serverConfig.getRocksDBLogLevel();
        if (rocksDBLogLevel.isPresent()) {
            options.setInfoLogLevel(rocksDBLogLevel.get());
        }

        options.setIncreaseParallelism(serverConfig.getRocksDBCompactionThreads());

        // Memtable size
        options.setWriteBufferSize(
                isCoreStore(storeName) ? serverConfig.getCoreMemtableSize() : serverConfig.getTimerMemtableSize());

        if (serverConfig.getGlobalRocksdbWriteBufferManager() != null) {
            options.setWriteBufferManager(serverConfig.getGlobalRocksdbWriteBufferManager());
        }

        // Streams default is 3
        options.setMaxWriteBufferNumber(4);
        options.setMinWriteBufferNumberToMerge(2);

        //  Concurrent jobs for both flushes and compaction
        options.setMaxBackgroundJobs(serverConfig.getRocksDBCompactionThreads());
        // Speeds up compaction by deploying sub compactions.
        // there may be max_background_jobs * max_subcompactions background threads running compaction
        options.setMaxSubcompactions(3);
        long rateLimit = serverConfig.getCoreStoreRateLimitBytes();
        if (rateLimit > 0) {
            options.setRateLimiter(new RateLimiter(
                    rateLimit,
                    RateLimiter.DEFAULT_REFILL_PERIOD_MICROS,
                    RateLimiter.DEFAULT_FAIRNESS,
                    RateLimiter.DEFAULT_MODE,
                    false));
        }
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
