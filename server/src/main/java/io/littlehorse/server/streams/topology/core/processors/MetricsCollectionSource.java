package io.littlehorse.server.streams.topology.core.processors;

/**
 * Determines the source used to collect partition metrics during punctuation.
 * <p>
 * STORE: metrics are read from the RocksDB state store (used on startup or when
 *        the in-memory batch couldn't finish within the time budget).
 * MEMORY: metrics are read from the in-memory accumulators (steady-state).
 */
enum MetricsCollectionSource {
    /** Read historical metrics from the persistent state store using the hint. */
    STORE,
    /** Read recent metrics from the in-memory accumulators. */
    MEMORY
}
