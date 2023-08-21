package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.SearchWfRunResponse;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchWfRunReply extends PublicScanReply<SearchWfRunResponse, WfRunId, WfRunIdModel> {

    public Class<SearchWfRunResponse> getProtoBaseClass() {
        return SearchWfRunResponse.class;
    }

    public Class<WfRunIdModel> getResultJavaClass() {
        return WfRunIdModel.class;
    }

    public Class<WfRunId> getResultProtoClass() {
        return WfRunId.class;
    }
    // public SearchWfRunResponse.Builder toProto() {
    // SearchWfRunResponse.Builder out = SearchWfRunResponse
    // .newBuilder()
    // .setCode(code);
    // if (message != null) out.setMessage(message);
    // if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

    // for (String objectId : objectIds) {
    // out.addIds(WfRun.parseId(objectId));
    // }

    // return out;
    // }

    // public void initFrom(Message proto) {
    // SearchWfRunResponse p = (SearchWfRunResponse) proto;
    // code = p.getCode();
    // if (p.hasMessage()) message = p.getMessage();
    // if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

    // for (WfRunId id : p.getIdsList()) {
    // objectIds.add(WfRun.getObjectId(id));
    // }
    // }
}
