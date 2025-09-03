package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.Env;
import org.rocksdb.IndexType;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.Options;
import org.rocksdb.Priority;
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

        switch (serverConfig.getServerMetricLevel().toLowerCase()) {
            case "debug":
            case "trace":
                options.setInfoLogLevel(InfoLogLevel.DEBUG_LEVEL);
                break;
            case "warn":
                options.setInfoLogLevel(InfoLogLevel.WARN_LEVEL);
            case "error":
                options.setInfoLogLevel(InfoLogLevel.ERROR_LEVEL);
                break;
            case "info":
            default:
                options.setInfoLogLevel(InfoLogLevel.INFO_LEVEL);
        }

        // Parallelism for Compactions and Flushing
        Env rocksEnv = Env.getDefault();
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        rocksEnv.setBackgroundThreads(threads, Priority.LOW);
        rocksEnv.setBackgroundThreads(threads, Priority.HIGH);
        options.setEnv(rocksEnv);
        options.setMaxBackgroundJobs(threads);
        options.setMaxSubcompactions(threads);

        // Configurations to avoid the "many small L0 files" problem
        options.setMaxWriteBufferNumber(4);
        options.setMinWriteBufferNumberToMerge(2);

        BlockBasedTableConfigWithAccessibleCache tableConfig =
                (BlockBasedTableConfigWithAccessibleCache) options.tableFormatConfig();

        // Partition the Index Filters to reduce stress on block cache.
        tableConfig.setFilterPolicy(new BloomFilter(10)); // 10 bits per key is default.
        tableConfig.setPartitionFilters(true);
        tableConfig.setIndexType(IndexType.kTwoLevelIndexSearch);
        tableConfig.setOptimizeFiltersForMemory(true);
        tableConfig.setBlockSize(BLOCK_SIZE);
        tableConfig.setCacheIndexAndFilterBlocks(true);

        // Memory limits
        if (serverConfig.getGlobalRocksdbBlockCache() != null) {
            // Streams provisions a *NON-shared* 50MB cache for every RocksDB instance. Need
            // to .close() it to avoid leaks so that we can provide a global one.
            Cache oldCache = tableConfig.blockCache();
            tableConfig.setBlockCache(serverConfig.getGlobalRocksdbBlockCache());
            oldCache.close();
        }
        if (serverConfig.getGlobalRocksdbWriteBufferManager() != null) {
            options.setWriteBufferManager(serverConfig.getGlobalRocksdbWriteBufferManager());
        }
        if (isCoreStore(storeName)) {
            options.setWriteBufferSize(serverConfig.getCoreMemtableSize());
        } else {
            // NOTE: We are experimenting with treating the timer store the same way as we treat
            // the core store.
            options.setWriteBufferSize(serverConfig.getCoreMemtableSize());
        }

        // Reduce read amplification
        options.setOptimizeFiltersForHits(false);

        // Save disk space a bit. This will also help (marginally) with Write Amplification.
        options.setCompressionType(CompressionType.ZSTD_COMPRESSION);

        // Compaction Configurations.
        if (serverConfig.getRocksDBUseLevelCompaction()) {
            // Level compaction has higher write amplification and lower read amplification.
            // Therefore, we use other configs to attempt to reduce WA.
            options.setCompactionStyle(CompactionStyle.LEVEL);

            // Configure gradual slowdowns of writes
            options.setLevel0FileNumCompactionTrigger(10); // Default 4. Larger compactions -> less WA.
            options.setLevel0SlowdownWritesTrigger(15); // Default 20. We want to avoid saturation.
            options.setLevel0StopWritesTrigger(36); // Default 36.

            // Configure how levels grow.
            options.setTargetFileSizeBase(64 * 1024L * 1024L); // 64MB, default.
            options.setMaxBytesForLevelBase(1024L * 512L); // 512MB in L1
            options.setMaxBytesForLevelMultiplier(20); // default 10; higher means lower Write Amp
        }

        if (serverConfig.useDirectIOForRocksDB()) {
            options.setUseDirectIoForFlushAndCompaction(true);
            options.setUseDirectReads(true);
        }

        long rateLimit = serverConfig.getCoreStoreRateLimitBytes();
        if (rateLimit > 0) {
            options.setRateLimiter(new RateLimiter(
                    rateLimit,
                    RateLimiter.DEFAULT_REFILL_PERIOD_MICROS,
                    RateLimiter.DEFAULT_FAIRNESS,
                    RateLimiter.DEFAULT_MODE,
                    false));
        }

        options.setTableFormatConfig(tableConfig);
    }

    @Override
    public void close(final String storeName, final Options options) {}

    private boolean isCoreStore(String storeName) {
        return !storeName.contains("timer");
    }
}
