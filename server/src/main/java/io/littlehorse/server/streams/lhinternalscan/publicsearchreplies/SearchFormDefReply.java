package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.objectId.FormDefIdModel;
import io.littlehorse.sdk.common.proto.FormDefId;
import io.littlehorse.sdk.common.proto.FormDefIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class SearchFormDefReply extends PublicScanReply<FormDefIdList, FormDefId, FormDefIdModel> {

    @Override
    public Class<FormDefId> getResultProtoClass() {
        return FormDefId.class;
    }

    @Override
    public Class<FormDefIdModel> getResultJavaClass() {
        return FormDefIdModel.class;
    }

    @Override
    public Class<FormDefIdList> getProtoBaseClass() {
        return FormDefIdList.class;
    }
}
