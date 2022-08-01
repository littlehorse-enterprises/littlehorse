package io.littlehorse.common.model;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;

public abstract class POSTable<T extends MessageOrBuilder> extends GETable<T> {
    public abstract long getLastUpdatedOffset();
    public abstract void setLastUpdatedOffset(long newOffset);

    public abstract void validateChange(POSTable<T> old)
        throws LHValidationError, LHConnectionError;
}
