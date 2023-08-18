package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.objectId.VariableIdModel;
import io.littlehorse.sdk.common.proto.SearchVariableResponse;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class SearchVariableReply
        extends PublicScanReply<SearchVariableResponse, VariableId, VariableIdModel> {

    public Class<SearchVariableResponse> getProtoBaseClass() {
        return SearchVariableResponse.class;
    }

    public Class<VariableIdModel> getResultJavaClass() {
        return VariableIdModel.class;
    }

    public Class<VariableId> getResultProtoClass() {
        return VariableId.class;
    }
}
