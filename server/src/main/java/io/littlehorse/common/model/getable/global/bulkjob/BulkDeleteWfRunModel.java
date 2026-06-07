package io.littlehorse.common.model.getable.global.bulkjob;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

import java.util.Date;

public class BulkDeleteWfRunModel extends LHSerializable<BulkDeleteWfRun> {

    private String wfSpecName;
    private Date earliestStart;
    private Date latestStart;
    private LHStatus wfRunStatus;

    @Override
    public BulkDeleteWfRun.Builder toProto() {
        BulkDeleteWfRun.Builder out = BulkDeleteWfRun.newBuilder();
        out.setWfSpecName(wfSpecName);
        out.setEarliestStart(LHUtil.fromDate(earliestStart));
        out.setLatestStart(LHUtil.fromDate(latestStart));
        if (wfRunStatus != null) {
            out.setWfRunStatus(wfRunStatus);
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        BulkDeleteWfRun p = (BulkDeleteWfRun) proto;
        this.wfSpecName = p.getWfSpecName();
        this.earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        this.latestStart = LHUtil.fromProtoTs(p.getLatestStart());
        if (p.hasWfRunStatus()) {
            this.wfRunStatus = p.getWfRunStatus();
        }
    }

    @Override
    public Class<BulkDeleteWfRun> getProtoBaseClass() {
        return BulkDeleteWfRun.class;
    }
}
