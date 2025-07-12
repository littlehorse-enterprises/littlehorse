package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.rocksdb.Cache;
import org.rocksdb.CompactionStyle;
import org.rocksdb.Env;
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

        LHServerConfig serverConfig = (LHServerConfig) configs.get(LH_SERVER_CONFIG_KEY);

        ///////////////////////////////////////////////////////////////////////
        // Block Cache Configuration
        ///////////////////////////////////////////////////////////////////////
        BlockBasedTableConfigWithAccessibleCache tableConfig =
                (BlockBasedTableConfigWithAccessibleCache) options.tableFormatConfig();
        if (serverConfig.getGlobalRocksdbBlockCache() != null) {
            // Streams provisions a *NON-shared* 50MB cache for every RocksDB instance. Need
            // to .close() it to avoid leaks so that we can provide a global one.
            Cache oldCache = tableConfig.blockCache();
            tableConfig.setBlockCache(serverConfig.getGlobalRocksdbBlockCache());
            tableConfig.setCacheIndexAndFilterBlocks(true);

            oldCache.close();
        }

        tableConfig.setOptimizeFiltersForMemory(OPTIMIZE_FILTERS_FOR_MEMORY);
        tableConfig.setBlockSize(BLOCK_SIZE);
        options.setTableFormatConfig(tableConfig);

        options.setOptimizeFiltersForHits(OPTIMIZE_FILTERS_FOR_HITS);

        ///////////////////////////////////////////////////////////////////////
        // Write Buffer Configuration
        ///////////////////////////////////////////////////////////////////////
        options.setMinWriteBufferNumberToMerge(2);
        options.setWriteBufferSize(
                isCoreStore(storeName) ? serverConfig.getCoreMemtableSize() : serverConfig.getTimerMemtableSize());
        if (serverConfig.getGlobalRocksdbWriteBufferManager() != null) {
            options.setWriteBufferManager(serverConfig.getGlobalRocksdbWriteBufferManager());
        }

        ///////////////////////////////////////////////////////////////////////
        // Compaction and Flushing
        ///////////////////////////////////////////////////////////////////////
        if (serverConfig.getRocksDBUseLevelCompaction()) {
            options.setCompactionStyle(CompactionStyle.LEVEL);
        } else {
            options.setCompactionStyle(CompactionStyle.UNIVERSAL);
        }

        // Direct I/O can be useful in containerized environments when the Host OS does
        // not clear page cache aggressively enough, which can cause OOM on containers
        // with low total memory limits.
        options.setUseDirectIoForFlushAndCompaction(serverConfig.useDirectIOForRocksDB());
        options.setUseDirectReads(serverConfig.useDirectIOForRocksDB());

        // See here: https://github.com/facebook/rocksdb/wiki/RocksDB-Tuning-Guide#parallelism-options
        // RocksDB recommends setting threads for compaction to be equal to number of cores. This is
        // separate from the number of *jobs* that can be running: parallelism vs concurrency.
        //
        // This is safer than using options.setIncreaseParallelism() because that config would end up
        // creating a thread pool for each separate rocksdb instance which causes an explosion in the
        // number of background threads.
        Env env = Env.getDefault();
        env.setBackgroundThreads(serverConfig.getRocksDBCompactionThreads());
        options.setEnv(env);

        // Max background jobs (flushes and compactions) that can be going on concurrently. These
        // jobs can be bottlenecked by I/O, so this number should be higher than the value
        // passed into `setIncreaseParallelism()`.
        options.setMaxBackgroundJobs(serverConfig.getRocksDBCompactionThreads() * 3);

        // Speeds up compaction by deploying sub compactions. A subcompaction is a "job" that
        // runs inside the background job threadpool (setIncreaseParallelism) and counts
        // against the setMaxBackgroundJobs().
        options.setMaxSubcompactions(3);

        ///////////////////////////////////////////////////////////////////////
        // Rate Limiter
        ///////////////////////////////////////////////////////////////////////
        long rateLimit = serverConfig.getCoreStoreRateLimitBytes();
        if (rateLimit > 0) {
            // The Rate Limiter limits the rate at which data can be written to disk by
            // flush and compaction. It does not limit the the rate at which we can call
            // `store.put()`.
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
