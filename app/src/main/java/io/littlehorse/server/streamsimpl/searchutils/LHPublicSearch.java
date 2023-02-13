package io.littlehorse.server.streamsimpl.searchutils;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.BookmarkPb;
import io.littlehorse.jlib.common.proto.GETableClassEnumPb;

public abstract class LHPublicSearch<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    protected BookmarkPb bookmark;
    protected Integer limit;

    public abstract GETableClassEnumPb getObjectType();

    public int getLimit() {
        if (limit == null) {
            limit = 100;
        }
        return limit;
    }

    protected abstract LHInternalSearch startInternalSearch(
        LHGlobalMetaStores stores
    ) throws LHValidationError;

    public LHInternalSearch getInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        LHInternalSearch out = startInternalSearch(stores);
        out.limit = getLimit();
        out.bookmark = bookmark;
        out.objectType = getObjectType();
        return out;
    }
}
