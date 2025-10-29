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

        // Level compaction has higher write amplification and lower read amplification.
        // Therefore, we use other configs to attempt to reduce WA.
        // Configurations to avoid the "many small L0 files" problem
        options.setCompactionStyle(CompactionStyle.LEVEL);
        options.setMaxWriteBufferNumber(3);
        options.setTargetFileSizeBase(128 * MB); // 64MB is default. We merge write buffers, so this is needed.
        options.setLevel0FileNumCompactionTrigger(12); // Default 4, higher means lower WA especially before KIP-1035
        options.setMaxBytesForLevelBase(128 * MB * 4); // Same as the compaction trigger
        options.setCompactionPriority(CompactionPriority.ByCompensatedSize); // Good for deletes / overwrites
        options.setBottommostCompressionType(CompressionType.LZ4_COMPRESSION);

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

        // Open the DB faster
        options.setSkipCheckingSstFileSizesOnDbOpen(true);
        options.setSkipStatsUpdateOnDbOpen(true);

        options.setTableFormatConfig(tableConfig);
    }

    @Override
    public void close(final String storeName, final Options options) {}
}
