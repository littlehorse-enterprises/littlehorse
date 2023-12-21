package io.littlehorse.server.streams.lhinternalscan.util;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;

public abstract class ScanBoundary<T extends Message> extends LHSerializable<T> {

    /**
     * Returns the start key that should be passed into the Tenant Model Store for
     * the range scan.
     * @return the start key that should be passed into the Tenant Model Store for the
     * range scan.
     */
    public abstract String getStartKey();

    /**
     * Returns the end key that should be passed into the Tenant Model Store for
     * the range scan.
     * @return the end key that should be passed into the Tenant Model Store for the
     * range scan.
     */
    public abstract String getEndKey();
}
