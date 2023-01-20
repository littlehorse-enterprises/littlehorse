package io.littlehorse.server.streamsimpl.searchutils;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.util.LHGlobalMetaStores;

public abstract class LHPublicSearch<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    protected BookmarkPb bookmark;
    protected Integer limit;

    public int getLimit() {
        if (limit == null) {
            return 100;
        } else {
            return limit;
        }
    }

    public abstract LHInternalSubSearch<?> getSubSearch(LHGlobalMetaStores stores);

    public abstract GETableClassEnumPb getObjectType();

    public LHInternalSearch getInternalSearch(LHGlobalMetaStores stores) {
        LHInternalSearch out = new LHInternalSearch();
        out.bookmark = bookmark;
        out.limit = getLimit();
        out.objectType = getObjectType();

        LHInternalSubSearch<?> subsearch = getSubSearch(stores);
        out.setSubsearch(subsearch);
        return out;
    }
}
