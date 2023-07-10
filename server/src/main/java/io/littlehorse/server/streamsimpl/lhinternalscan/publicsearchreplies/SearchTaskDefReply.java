package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.TaskDefIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchTaskDefReply
    extends PublicScanReply<SearchTaskDefReplyPb, TaskDefIdPb, TaskDefId> {

    public Class<SearchTaskDefReplyPb> getProtoBaseClass() {
        return SearchTaskDefReplyPb.class;
    }

    public Class<TaskDefId> getResultJavaClass() {
        return TaskDefId.class;
    }

    public Class<TaskDefIdPb> getResultProtoClass() {
        return TaskDefIdPb.class;
    }
    // public SearchTaskDefReplyPb.Builder toProto() {
    //     SearchTaskDefReplyPb.Builder out = SearchTaskDefReplyPb
    //         .newBuilder()
    //         .setCode(code);
    //     if (message != null) out.setMessage(message);
    //     if (bookmark != null) out.setBookmark(ByteString.copyFrom(bookmark));

    //     for (String objectId : objectIds) {
    //         out.addIds(TaskDef.parseId(objectId));
    //     }

    //     return out;
    // }

    // public void initFrom(Message proto) {
    //     SearchTaskDefReplyPb p = (SearchTaskDefReplyPb) proto;
    //     code = p.getCode();
    //     if (p.hasMessage()) message = p.getMessage();
    //     if (p.hasBookmark()) bookmark = p.getBookmark().toByteArray();

    //     for (TaskDefIdPb id : p.getIdsList()) {
    //         objectIds.add(TaskDef.getObjectId(id));
    //     }
    // }
}
