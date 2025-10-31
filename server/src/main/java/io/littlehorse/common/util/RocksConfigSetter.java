package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.CompactionOptionsUniversal;
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

        if (storeName.contains("timer")) {
            // Timer stores rely on range scans a lot and have less data which is more short-lived,
            // so we rely on the Level compaction style.
            options.setCompactionStyle(CompactionStyle.LEVEL);
            options.setTargetFileSizeBase(128 * MB);
            options.setMaxBytesForLevelBase(128 * MB * 12);

        } else {
            // Core stores are very write-heavy and have fewer range scans, so we use Universal.
            options.setCompactionStyle(CompactionStyle.UNIVERSAL);
            options.setCompressionType(CompressionType.LZ4_COMPRESSION);

            CompactionOptionsUniversal cou = new CompactionOptionsUniversal();
            cou.setAllowTrivialMove(true).setMinMergeWidth(4);
            options.setCompactionOptionsUniversal(cou);
            cou.close();

            // See: https://github.com/facebook/rocksdb/wiki/universal-compaction#db-column-family-size-if-num_levels
            options.setNumLevels(10);
        }

        options.setLevel0FileNumCompactionTrigger(12); // Default 4, higher means lower WA especially before KIP-1035
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

        // Open the DB faster
        options.setSkipCheckingSstFileSizesOnDbOpen(true);
        options.setSkipStatsUpdateOnDbOpen(true);

        options.setTableFormatConfig(tableConfig);
    }

    @Override
    public void close(final String storeName, final Options options) {}
}
