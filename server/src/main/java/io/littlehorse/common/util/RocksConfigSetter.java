package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.CompactionOptionsUniversal;
import org.rocksdb.CompactionStyle;
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
    public static final String LH_SERVER_CONFIG_KEY = "LH_SERVER_CONFIG";

    static final long KB = 1024L;
    static final long MB = KB * KB;

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
        log.trace("Overriding rocksdb settings for store {}", storeName);

        LHServerConfig serverConfig = (LHServerConfig) configs.get(LH_SERVER_CONFIG_KEY);

        // Parallelism for Compactions and Flushing
        Env rocksEnv = options.getEnv();
        if (serverConfig.getRocksDBCompactionThreads().isPresent()) {
            rocksEnv.setBackgroundThreads(
                    serverConfig.getRocksDBCompactionThreads().get(), Priority.LOW);
        }
        if (serverConfig.getRocksDBFlushThreads().isPresent()) {
            rocksEnv.setBackgroundThreads(serverConfig.getRocksDBFlushThreads().get(), Priority.HIGH);
        }
        if (serverConfig.getRocksDBBackgroundJobs().isPresent()) {
            options.setMaxBackgroundJobs(serverConfig.getRocksDBBackgroundJobs().get());
        }
        if (serverConfig.getRocksDBMaxSubcompactions().isPresent()) {
            options.setMaxSubcompactions(
                    serverConfig.getRocksDBMaxSubcompactions().get());
        }

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
        options.setWriteBufferSize(serverConfig.getCoreMemtableSize());

        // Limiting Memory
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
        if (serverConfig.getRocksDBOptimizeFiltersForHits().isPresent()) {
            options.setOptimizeFiltersForHits(
                    serverConfig.getRocksDBOptimizeFiltersForHits().get());
        }

        // Compaction configs
        if (serverConfig.getRocksDBCompactionStyle().isPresent()) {
            options.setCompactionStyle(serverConfig.getRocksDBCompactionStyle().get());
        }
        if (serverConfig.getRocksDBLevel0FileNumCompactionTrigger().isPresent()) {
            options.setLevel0FileNumCompactionTrigger(
                    serverConfig.getRocksDBLevel0FileNumCompactionTrigger().get());
        }
        if (serverConfig.getRocksDBLevel0SlowdownWritesTrigger().isPresent()) {
            options.setLevel0SlowdownWritesTrigger(
                    serverConfig.getRocksDBLevel0SlowdownWritesTrigger().get());
        }
        if (serverConfig.getRocksDBLevel0StopWritesTrigger().isPresent()) {
            options.setLevel0StopWritesTrigger(
                    serverConfig.getRocksDBLevel0StopWritesTrigger().get());
        }
        if (serverConfig.getRocksDBCompressionType().isPresent()) {
            options.setCompressionType(serverConfig.getRocksDBCompressionType().get());
        }
        if (serverConfig.getRocksDBSoftPendingCompactionBytesLimit().isPresent()) {
            options.setSoftPendingCompactionBytesLimit(
                    serverConfig.getRocksDBSoftPendingCompactionBytesLimit().get());
        }
        if (serverConfig.getRocksDBDelayedWriteRateBytes().isPresent()) {
            options.setDelayedWriteRate(
                    serverConfig.getRocksDBDelayedWriteRateBytes().get());
        }
        if (serverConfig.getRocksDBMaxWriteBufferNumber().isPresent()) {
            options.setMaxWriteBufferNumber(
                    serverConfig.getRocksDBMaxWriteBufferNumber().get());
        }
        if (serverConfig.getRocksDBTargetFileSizeBytes().isPresent()) {
            options.setTargetFileSizeBase(
                    serverConfig.getRocksDBTargetFileSizeBytes().get());
        }

        if (options.compactionStyle() == CompactionStyle.LEVEL) {
            if (serverConfig.getRocksDBCompactionPriority().isPresent()) {
                options.setCompactionPriority(
                        serverConfig.getRocksDBCompactionPriority().get());
            }
            if (serverConfig.getRocksDBDynamicLevelBytes().isPresent()) {
                options.setLevelCompactionDynamicLevelBytes(
                        serverConfig.getRocksDBDynamicLevelBytes().get());
            }
            if (serverConfig.getRocksDBTargetBaseLevelSizeBytes().isPresent()) {
                options.setMaxBytesForLevelBase(
                        serverConfig.getRocksDBTargetBaseLevelSizeBytes().get());
            }
            if (serverConfig.getRocksDBLevelSizeMultiplier().isPresent()) {
                options.setMaxBytesForLevelMultiplier(
                        serverConfig.getRocksDBLevelSizeMultiplier().get());
            }
        } else if (options.compactionStyle() == CompactionStyle.UNIVERSAL) {
            try (CompactionOptionsUniversal cou = new CompactionOptionsUniversal()) {
                if (serverConfig.getRocksDBMinMergeWidth().isPresent()) {
                    cou.setMinMergeWidth(serverConfig.getRocksDBMinMergeWidth().get());
                }
                if (serverConfig
                        .getRocksDBUniversalCompactionCompressionSizePercent()
                        .isPresent()) {
                    cou.setCompressionSizePercent(serverConfig
                            .getRocksDBUniversalCompactionCompressionSizePercent()
                            .get());
                }
                options.setCompactionOptionsUniversal(cou);
            }
        }

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
