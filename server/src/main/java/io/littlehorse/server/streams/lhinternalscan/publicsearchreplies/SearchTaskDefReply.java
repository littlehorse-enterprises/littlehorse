package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.sdk.common.proto.SearchTaskDefResponse;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchTaskDefReply extends PublicScanReply<SearchTaskDefResponse, TaskDefId, TaskDefIdModel> {

    public Class<SearchTaskDefResponse> getProtoBaseClass() {
        return SearchTaskDefResponse.class;
    }

    public Class<TaskDefIdModel> getResultJavaClass() {
        return TaskDefIdModel.class;
    }

    public Class<TaskDefId> getResultProtoClass() {
        return TaskDefId.class;
    }
    // public SearchTaskDefResponse.Builder toProto() {
    // SearchTaskDefResponse.Builder out = SearchTaskDefResponse
    // .newBuilder()
    // .setCode(code);
    // if (message != null) out.setMessage(message);
    // if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

    // for (String objectId : objectIds) {
    // out.addIds(TaskDef.parseId(objectId));
    // }

    // return out;
    // }

    // public void initFrom(Message proto) {
    // SearchTaskDefResponse p = (SearchTaskDefResponse) proto;
    // code = p.getCode();
    // if (p.hasMessage()) message = p.getMessage();
    // if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

    // for (TaskDefId id : p.getIdsList()) {
    // objectIds.add(TaskDef.getObjectId(id));
    // }
    // }
}
