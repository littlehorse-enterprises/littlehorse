package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHEnumValueDef;

/**
 * The priority of a support ticket.
 *
 * <p>LittleHorse compares Enum members by their backing values. LOW is 0 and MEDIUM is 1. HIGH
 * has the explicit backing value 10, so CRITICAL is 11.
 */
public enum Priority {
    LOW,
    MEDIUM,
    @LHEnumValueDef(10)
    HIGH,
    CRITICAL
}
