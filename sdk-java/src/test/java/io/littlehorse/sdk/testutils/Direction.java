package io.littlehorse.sdk.testutils;

import io.littlehorse.sdk.worker.LHEnumValueDef;

public enum Direction {
    NORTH,
    @LHEnumValueDef(10)
    EAST,
    SOUTH
}
