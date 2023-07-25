package io.littlehorse.server.streamsimpl.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import java.util.List;

/**
 * T : The protobuf for the PublicScanRequest
 * RP: The Response Protobuf
 * OP: The Individual Entry Protobuf
 * OJ: The Individual Entry Java Object
 * R : The protobuf for the resulting response
 */
public abstract class PublicScanRequest<
    T extends Message, // This is the actual incoming search proto
    RP extends Message,
    OP extends Message,
    OJ extends LHSerializable<OP>,
    R extends PublicScanReply<RP, OP, OJ>
>
    extends LHSerializable<T> {

    protected BookmarkPb bookmark;
    protected Integer limit;
    protected GetableSearch getableSearch;

    public abstract GetableClassEnumPb getObjectType();

    public int getLimit() {
        if (limit == null) {
            limit = 100;
        }
        return limit;
    }

    @Deprecated
    protected abstract InternalScan startInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError;

    public InternalScan getInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        SearchScanBoundaryStrategy searchScanBoundaryStrategy = getScanBoundary(
            getSearchAttributeString()
        );
        if (searchScanBoundaryStrategy != null) {
            getableSearch =
                new GetableSearchImpl(getObjectType(), searchScanBoundaryStrategy);
        }
        InternalScan out;
        if (getableSearch == null) {
            out = startInternalSearch(stores);
        } else {
            out = getableSearch.buildInternalScan(stores, indexTypeForSearch());
        }
        if (out.limit == 0) out.limit = getLimit();
        out.bookmark = bookmark;
        out.objectType = getObjectType();
        return out;
    }

    /**
     * Retrieves the attribute string used for search operations. The attribute string is intended to be used by the
     * {@link io.littlehorse.server.streamsimpl.BackendInternalComms#doScan(InternalScan)} method to perform scans over
     * stored tags.
     *
     * @return The attribute string in the format:
     * VARIABLE/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_21.0
     *
     * @throws LHValidationError if there are invalid options in the input arguments.
     */
    public String getSearchAttributeString() throws LHValidationError {
        return Tag.getAttributeString(getObjectType(), getSearchAttributes());
    }

    /**
     * Builds search attributes based on the provided search input arguments.
     * This method is intended to be overridden by subclasses to implement custom logic.
     *
     * @return {@link Attribute} containing attributes associated with the search operation.
     *
     * @throws LHValidationError if there are invalid options in the input arguments.
     */
    public List<Attribute> getSearchAttributes() throws LHValidationError {
        return List.of();
    }

    /**
     * Returns the storage type to be used by this search operation.
     * @return The storage type or null if not specified in the configuration.
     * @throws LHValidationError if there are validation errors in the input.
     */

    public abstract TagStorageTypePb indexTypeForSearch() throws LHValidationError;

    /**
     * Validate input parameters for the search operation
     * @throws LHValidationError if there are validation errors in the input.
     */
    public abstract void validate() throws LHValidationError;

    public abstract SearchScanBoundaryStrategy getScanBoundary(
        String searchAttributeString
    );
}
