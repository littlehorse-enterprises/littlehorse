package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.CompactionPriority;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.Env;
import org.rocksdb.IndexShorteningMode;
import org.rocksdb.IndexType;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.Options;
import org.rocksdb.Priority;

@Slf4j
public class RocksConfigSetter implements RocksDBConfigSetter {

    // This key is used to inject the LHServerConfig into the Map<String, Object> configs
    // passed into the setConfig() method.
    public static final String LH_SERVER_CONFIG_KEY = "obiwan.kenobi";

    static final long KB = 1024L;
    static final long MB = KB * KB;

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
        log.trace("Overriding rocksdb settings for store {}", storeName);

        LHServerConfig serverConfig = (LHServerConfig) configs.get(LH_SERVER_CONFIG_KEY);

        // Parallelism for Compactions and Flushing
        Env rocksEnv = Env.getDefault();
        int threads = serverConfig.getRocksDBCompactionThreads();
        rocksEnv.setBackgroundThreads(threads, Priority.LOW);
        rocksEnv.setBackgroundThreads(threads, Priority.HIGH);
        options.setEnv(rocksEnv);
        options.setMaxBackgroundJobs(threads); // Rocksdb tuning guide recommendation
        options.setMaxSubcompactions(3);

        // Info LOG file configurations
        switch (serverConfig.getServerMetricLevel()) {
            case "TRACE":
                // Trace is the lowest level available in LH Server, so we map
                // it to the lowest level in RocksDB (DEBUG)
                options.setInfoLogLevel(InfoLogLevel.DEBUG_LEVEL);
                break;
            case "DEBUG":
                options.setInfoLogLevel(InfoLogLevel.DEBUG_LEVEL);
                break;
            default:
                options.setInfoLogLevel(InfoLogLevel.INFO_LEVEL);
        }
        // Keep 14 days of logs, rolling into a new file every day.
        options.setLogFileTimeToRoll(Duration.ofDays(1).toSeconds());
        options.setKeepLogFileNum(14);

        BlockBasedTableConfigWithAccessibleCache tableConfig =
                (BlockBasedTableConfigWithAccessibleCache) options.tableFormatConfig();

        tableConfig.setFilterPolicy(new BloomFilter(10)); // 10 bits per key is default.
        tableConfig.setOptimizeFiltersForMemory(true);
        tableConfig.setBlockSize(64 * KB);
        tableConfig.setPinL0FilterAndIndexBlocksInCache(true);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        tableConfig.setCacheIndexAndFilterBlocksWithHighPriority(true);
        tableConfig.setIndexType(IndexType.kBinarySearch);
        tableConfig.setIndexShortening(IndexShorteningMode.kShortenSeparatorsAndSuccessor);
        options.setOptimizeFiltersForHits(false);
        options.setWriteBufferSize(serverConfig.getCoreMemtableSize());

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

        // Use level compaction in order to keep predictable range scan performance for searches, and
        // to create more predictable compaction workloads.
        options.setCompactionStyle(CompactionStyle.LEVEL);
        options.setCompressionType(CompressionType.LZ4_COMPRESSION);
        options.setLevel0FileNumCompactionTrigger(6);
        options.setLevel0SlowdownWritesTrigger(20); // default
        Long softLimit = serverConfig.getRocksDBPendingCompactionBytesSoftLimit();
        if (softLimit != null) {
            options.setSoftPendingCompactionBytesLimit(softLimit);
        }
        options.setCompactionPriority(CompactionPriority.MinOverlappingRatio);

        options.setTargetFileSizeBase(32 * MB);
        options.setMaxWriteBufferNumber(3);

        // I/O Configurations
        options.setAdviseRandomOnOpen(true);
        options.setCompactionReadaheadSize(256 * KB); // max size for a single GP3 read on EBS
        if (serverConfig.useDirectIOForRocksDB()) {
            options.setUseDirectIoForFlushAndCompaction(true);
            options.setUseDirectReads(true);
        } else {
            options.setBytesPerSync(1 * MB); // https://github.com/facebook/rocksdb/wiki/IO#range-sync
        }

        if (serverConfig.getGlobalRocksdbRateLimiter() != null) {
            options.setRateLimiter(serverConfig.getGlobalRocksdbRateLimiter());
        }
        serverConfig.getRocksDBDelayedWriteRateBytes().ifPresent(options::setDelayedWriteRate);

        // Open the DB faster
        options.setSkipCheckingSstFileSizesOnDbOpen(true);
        options.setSkipStatsUpdateOnDbOpen(true);
        options.setMaxManifestFileSize(4 * MB);

        options.setTableFormatConfig(tableConfig);
    }

    @Override
    public void close(final String storeName, final Options options) {}
}
