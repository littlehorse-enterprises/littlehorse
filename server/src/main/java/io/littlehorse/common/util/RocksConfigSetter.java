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
        options.setAdviseRandomOnOpen(true);

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
        // tableConfig.setBlockSize(serverConfig.getRocksDBBlockSize());
        tableConfig.setBlockSize(64 * 1024L);
        tableConfig.setPinL0FilterAndIndexBlocksInCache(true);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        tableConfig.setCacheIndexAndFilterBlocksWithHighPriority(true);
        if (storeName.contains("timer")) {
            tableConfig.setIndexShortening(IndexShorteningMode.kNoShortening);
            tableConfig.setIndexType(IndexType.kBinarySearchWithFirstKey);
            options.setWriteBufferSize(serverConfig.getTimerMemtableSize());
        } else {
            tableConfig.setIndexType(IndexType.kBinarySearch);
            tableConfig.setIndexShortening(IndexShorteningMode.kShortenSeparatorsAndSuccessor);
            options.setWriteBufferSize(serverConfig.getCoreMemtableSize());
        }

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
        options.setCompactionStyle(CompactionStyle.LEVEL);
        options.setTargetFileSizeBase(128 * 1024L * 1024L); // 64MB is default.
        options.setMaxBytesForLevelBase(1024L * 1024L * 128 * 4); // Same as the compaction trigger
        options.setMaxBytesForLevelMultiplier(8); // default 10; higher means lower Write Amp but bigger compactions
        options.setCompactionReadaheadSize(256_000); // max size for a single GP3 read on EBS
        options.setCompactionPriority(CompactionPriority.MinOverlappingRatio); // lower write amp
        options.setOptimizeFiltersForHits(false);
        options.setBottommostCompressionType(CompressionType.LZ4_COMPRESSION);

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

        if (serverConfig.getGlobalRocksdbRateLimiter() != null) {
            options.setRateLimiter(serverConfig.getGlobalRocksdbRateLimiter());
        }

        options.setTableFormatConfig(tableConfig);
    }

    @Override
    public void close(final String storeName, final Options options) {}
}
