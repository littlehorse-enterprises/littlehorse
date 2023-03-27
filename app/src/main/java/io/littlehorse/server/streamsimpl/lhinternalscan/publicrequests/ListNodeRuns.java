package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.ListNodeRunsPb;
import io.littlehorse.jlib.common.proto.ListNodeRunsReplyPb;
import io.littlehorse.jlib.common.proto.NodeRunPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListNodeRunsReply;

public class ListNodeRuns
    extends PublicScanRequest<ListNodeRunsPb, ListNodeRunsReplyPb, NodeRunPb, NodeRun, ListNodeRunsReply> {

    public String wfRunId;

    public Class<ListNodeRunsPb> getProtoBaseClass() {
        return ListNodeRunsPb.class;
    }

    public ListNodeRunsPb.Builder toProto() {
        return ListNodeRunsPb.newBuilder().setWfRunId(wfRunId);
    }

    public void initFrom(Message proto) {
        ListNodeRunsPb p = (ListNodeRunsPb) proto;
        wfRunId = p.getWfRunId();
    }

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.NODE_RUN;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();
        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT;
        out.type = ScanBoundaryCase.OBJECT_ID_PREFIX;
        out.partitionKey = wfRunId;
        out.objectIdPrefix = wfRunId + "/";
        return out;
    }
}
