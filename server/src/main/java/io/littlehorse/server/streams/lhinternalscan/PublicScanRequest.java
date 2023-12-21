package io.littlehorse.server.streams.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.scanfilter.ScanFilterModel;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.List;

/**
 * T : The protobuf for the PublicScanRequest RP: The Response Protobuf OP: The
 * Individual Entry
 * Protobuf OJ: The Individual Entry Java Object R : The protobuf for the
 * resulting response
 */
public abstract class PublicScanRequest<
                T extends Message, // This is the actual incoming search proto
                RP extends Message,
                OP extends Message,
                OJ extends LHSerializable<OP>,
                R extends PublicScanReply<RP, OP, OJ>>
        extends LHSerializable<T> {

    protected BookmarkPb bookmark;
    protected Integer limit;

    public abstract GetableClassEnum getObjectType();

    /**
     * Whether we should return a list of objects or a list of objct id's to the caller.
     * You should override this method if you want to return a list of objects.
     * @return whether to return object id's or objects
     */
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT_ID;
    }

    public abstract LHStoreType getStoreType();

    public int getLimit() {
        if (limit == null) {
            limit = 100;
        }
        return limit;
    }

    public InternalScanRequestModel<?> getInternalSearch(RequestExecutionContext ctx) throws LHApiException {
        ScanBoundary<?, ?> scanBoundary = getScanBoundary(ctx);
        InternalScanRequestModel<?> out = new InternalScanRequestModel(scanBoundary, ctx);

        out.setResultType(getResultType());
        if (out.getLimit() == 0) out.setLimit(getLimit());
        out.setBookmark(bookmark);
        out.setStoreName(getStoreType().name());
        out.setFilters(getFilters(ctx));
        return out;
    }

    /**
     * Returns a ScanBoundary object that provides start and end boundaries for the specific scan.
     * @param ctx is the RequestExecutionContext.
     * @return a ScanBoundary.
     * @throws LHApiException if the search parameters specified by the client are invalid.
     */
    public abstract ScanBoundary<?, ?> getScanBoundary(RequestExecutionContext ctx) throws LHApiException;

    /**
     * This method can be overriden to specify filters that filter out results returned by the
     * range scan over the `getScanBoundary()`. Only entries satisfying all of the filters will
     * be returned in the final result to the client.
     * @param ctx is a RequestExecutionContext.
     * @return a list of filters to apply to each request.
     */
    public List<ScanFilterModel> getFilters(RequestExecutionContext ctx) {
        return List.of();
    }
}
