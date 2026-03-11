package io.littlehorse.sdk.worker;

/**
 * Reason code associated with a task worker health snapshot.
 */
public enum LHTaskWorkerHealthReason {
    /** Worker is not healthy and should not accept task execution. */
    UNHEALTHY,
    /** Worker is waiting during server rebalance and is temporarily unavailable. */
    SERVER_REBALANCING,
    /** Worker is healthy and ready to execute tasks. */
    HEALTHY
}
