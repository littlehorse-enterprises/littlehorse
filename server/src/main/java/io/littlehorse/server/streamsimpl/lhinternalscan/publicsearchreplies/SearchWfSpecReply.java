package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.SearchWfSpecResponse;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchWfSpecReply
    extends PublicScanReply<SearchWfSpecResponse, WfSpecId, WfSpecIdModel> {

    public Class<SearchWfSpecResponse> getProtoBaseClass() {
        return SearchWfSpecResponse.class;
    }

    public Class<WfSpecIdModel> getResultJavaClass() {
        return WfSpecIdModel.class;
    }

    public Class<WfSpecId> getResultProtoClass() {
        return WfSpecId.class;
    }
    // public SearchWfSpecResponse.Builder toProto() {
    //     SearchWfSpecResponse.Builder out = SearchWfSpecResponse
    //         .newBuilder()
    //         .setCode(code);
    //     if (message != null) out.setMessage(message);
    //     if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

    //     for (String objectId : objectIds) {
    //         out.addIds(WfSpec.parseId(objectId));
    //     }

    //     return out;
    // }

    // public void initFrom(Message proto) {
    //     SearchWfSpecResponse p = (SearchWfSpecResponse) proto;
    //     code = p.getCode();
    //     if (p.hasMessage()) message = p.getMessage();
    //     if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

    //     for (WfSpecId id : p.getIdsList()) {
    //         objectIds.add(WfSpec.getObjectId(id));
    //     }
    // }
}
