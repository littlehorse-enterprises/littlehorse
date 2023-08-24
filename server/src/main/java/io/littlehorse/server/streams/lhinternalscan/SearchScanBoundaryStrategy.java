package io.littlehorse.server.streams.lhinternalscan;

import com.google.protobuf.Message;

/**
 * Represents the search scan boundary mechanism to be utilized in the Search by
 * {@link
 * io.littlehorse.server.streams.BackendInternalComms#doScan(InternalScan)}
 * method.
 */
public interface SearchScanBoundaryStrategy {
    /**
     * Builds a specific type of scan boundary.
     *
     * @return Scan Boundary proto representation as defined in {@link
     *         io.littlehorse.common.proto.InternalScanPb}.
     */
    Message buildScanProto();

    /**
     * Retrieves the attribute string used for the search operation.
     *
     * @return The attribute string.
     */
    String getSearchAttributeString();
}
