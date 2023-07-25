package io.littlehorse.server.streamsimpl.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
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

    public abstract GetableClassEnumPb getObjectType();

    public int getLimit() {
        if (limit == null) {
            limit = 100;
        }
        return limit;
    }

    protected abstract InternalScan startInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError;

    public InternalScan getInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        InternalScan out = startInternalSearch(stores);
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
     * Build search attributes from search input arguments
     * @return {@link Attribute} attributes associated to search operation
     * @throws LHValidationError if there are invalid options in the input arguments.
     */
    public List<Attribute> getSearchAttributes() throws LHValidationError {
        return List.of();
    }
}
