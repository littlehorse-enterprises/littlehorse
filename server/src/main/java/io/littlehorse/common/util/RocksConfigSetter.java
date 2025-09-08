package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.CompactionPriority;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionOptions;
import org.rocksdb.CompressionType;
import org.rocksdb.Env;
import org.rocksdb.IndexType;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.Options;
import org.rocksdb.Priority;

@Slf4j
public class RocksConfigSetter implements RocksDBConfigSetter {

    // This key is used to inject the LHServerConfig into the Map<String, Object> configs
    // passed into the setConfig() method.
    public static final String LH_SERVER_CONFIG_KEY = "obiwan.kenobi";

    // From RocksDB docs: default is 4kb, but Facebook tends to use 16-32kb in production.
    // With longer keys (we have a lot of long keys), higher block size is recommended.
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

        // Parallelism for Compactions and Flushing
        Env rocksEnv = Env.getDefault();
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        rocksEnv.setBackgroundThreads(threads, Priority.LOW);
        rocksEnv.setBackgroundThreads(threads, Priority.HIGH);
        options.setEnv(rocksEnv);
        options.setMaxBackgroundJobs(6); // Rocksdb tuning guide recommendation
        options.setMaxSubcompactions(3);

        // Configurations to avoid the "many small L0 files" problem
        options.setMaxWriteBufferNumber(4);
        options.setMinWriteBufferNumberToMerge(2);

        switch (serverConfig.getServerMetricLevel()) {
            case "TRACE":
                // Trace is the lowest level available in LH Server, so we map
                // it to the lowest level in RocksDB (DEBUG)
                options.setInfoLogLevel(InfoLogLevel.DEBUG_LEVEL);
                break;
            case "DEBUG":
                // The second lowest level in LH is "DEBUG", so we set it to the
                // second-lowest level in RocksDB (INFO)
                options.setInfoLogLevel(InfoLogLevel.INFO_LEVEL);
                break;
            default:
                options.setInfoLogLevel(InfoLogLevel.WARN_LEVEL);
        }

        BlockBasedTableConfigWithAccessibleCache tableConfig =
                (BlockBasedTableConfigWithAccessibleCache) options.tableFormatConfig();

        tableConfig.setFilterPolicy(new BloomFilter(10)); // 10 bits per key is default.
        tableConfig.setOptimizeFiltersForMemory(true);
        tableConfig.setBlockSize(BLOCK_SIZE);
        tableConfig.setPinL0FilterAndIndexBlocksInCache(true);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        tableConfig.setCacheIndexAndFilterBlocksWithHighPriority(true);
        tableConfig.setPartitionFilters(true);
        tableConfig.setIndexType(IndexType.kTwoLevelIndexSearch);
        options.setAllowMmapReads(false);
        options.setAllowMmapWrites(false);

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
        // NOTE: We are experimenting with treating the timer store the same way as we treat
        // the core store.
        options.setWriteBufferSize(serverConfig.getCoreMemtableSize());
        options.setOptimizeFiltersForHits(false);

        // Compress the bottom level only, which contains about 90% of the database,
        // but shouldn't be involved in most of the write paths and I/O.
        options.setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION);
        CompressionOptions compressionOptions = new CompressionOptions();
        // Reduce ZSTD compression level to be faster at the cost of less disk savings.
        compressionOptions.setLevel(1);
        options.setCompressionOptions(compressionOptions);
        compressionOptions.close();

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
            options.setMaxBytesForLevelBase(1024L * 1024L * 512L); // 512MB in L1
            options.setMaxBytesForLevelMultiplier(20); // default 10; higher means lower Write Amp
        }

        // I/O Configurations
        if (serverConfig.useDirectIOForRocksDB()) {
            options.setUseDirectIoForFlushAndCompaction(true);
            options.setUseDirectReads(true);
        } else {
            // Periodically sync the bytes in the background. This reduces burstiness of the I/O, which
            // reduces tail latency (and can also reduce overall page cache usage when buffered I/O is
            // used).
            //
            // References:
            // - https://github.com/facebook/rocksdb/wiki/Setup-Options-and-Basic-Tuning#other-general-options
            // - https://github.com/facebook/rocksdb/wiki/IO#range-sync
            options.setBytesPerSync(1024L * 1024L);
        }

        // Reduce write amplification compared to default. Also recommended by RocksDB Tuning Guide
        options.setCompactionPriority(CompactionPriority.MinOverlappingRatio);

        if (serverConfig.getGlobalRocksdbRateLimiter() != null) {
            options.setRateLimiter(serverConfig.getGlobalRocksdbRateLimiter());
        }

        options.setTableFormatConfig(tableConfig);
    }

    @Override
    public void close(final String storeName, final Options options) {}
}
