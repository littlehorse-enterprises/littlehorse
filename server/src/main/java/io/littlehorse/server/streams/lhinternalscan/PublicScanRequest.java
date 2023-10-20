package io.littlehorse.server.streams.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
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
    protected GetableSearch getableSearch;

    public abstract GetableClassEnum getObjectType();

    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT_ID;
    }

    public abstract LHStore getStoreType();

    public int getLimit() {
        if (limit == null) {
            limit = 100;
        }
        return limit;
    }

    public InternalScan getInternalSearch(ReadOnlyMetadataProcessorDAO readOnlyDao) throws LHApiException {
        SearchScanBoundaryStrategy searchScanBoundaryStrategy = getScanBoundary(getSearchAttributeString());
        getableSearch = new GetableSearchImpl(getObjectType(), searchScanBoundaryStrategy);
        InternalScan out = getableSearch.buildInternalScan(readOnlyDao, indexTypeForSearch(readOnlyDao));
        if (out.limit == 0) out.limit = getLimit();
        out.bookmark = bookmark;

        out.objectType = getObjectType();

        out.resultType = getResultType();

        out.storeName = getStoreType().getStoreName();
        return out;
    }

    /**
     * Retrieves the attribute string used for search operations. The attribute
     * string is intended
     * to be used by the {@link
     * io.littlehorse.server.streams.BackendInternalComms#doScan(InternalScan)}
     * method to
     * perform scans over stored tags.
     *
     * @return The attribute string in the format:
     *         VARIABLE/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_21.0
     * @throws LHApiException if there are invalid options in the input
     *                           arguments.
     */
    public String getSearchAttributeString() throws LHApiException {
        return Tag.getAttributeString(getObjectType(), getSearchAttributes());
    }

    /**
     * Builds search attributes based on the provided search input arguments. This
     * method is
     * intended to be overridden by subclasses to implement custom logic.
     *
     * @return {@link Attribute} containing attributes associated with the search
     *         operation.
     * @throws LHApiException if there are invalid options in the input
     *                           arguments.
     */
    public List<Attribute> getSearchAttributes() throws LHApiException {
        return List.of();
    }

    /**
     * Returns the storage type to be used by this search operation.
     *
     * @return The storage type or null if not specified in the configuration.
     * @throws LHApiException if there are validation errors in the input.
     */
    public abstract TagStorageType indexTypeForSearch(ReadOnlyMetadataProcessorDAO readOnlyDao) throws LHApiException;

    public abstract SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException;
}
