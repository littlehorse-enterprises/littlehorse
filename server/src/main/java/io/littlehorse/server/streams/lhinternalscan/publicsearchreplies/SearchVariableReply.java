package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchVariableReply extends PublicScanReply<VariableIdList, VariableId, VariableIdModel> {

    public Class<VariableIdList> getProtoBaseClass() {
        return VariableIdList.class;
    }

    public Class<VariableIdModel> getResultJavaClass() {
        return VariableIdModel.class;
    }

    public Class<VariableId> getResultProtoClass() {
        return VariableId.class;
    }
}
