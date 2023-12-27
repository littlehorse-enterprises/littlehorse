package io.littlehorse.server.streams.lhinternalscan.util;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.ObjectIdModel;

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

    /**
     * Returns the type of thing that we scan over.
     * @return the type of thing we scan over.
     */
    public abstract Class<? extends Storeable<?>> getIterType();
}
