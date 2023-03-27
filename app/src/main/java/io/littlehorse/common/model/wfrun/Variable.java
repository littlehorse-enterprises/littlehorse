package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.VariablePb;
import java.util.Date;

public class Variable extends GETable<VariablePb> {

    public VariableValue value;
    public String wfRunId;
    public int threadRunNumber;
    public String name;
    public Date date;

    public Class<VariablePb> getProtoBaseClass() {
        return VariablePb.class;
    }

    public void initFrom(Message proto) {
        VariablePb p = (VariablePb) proto;
        value = VariableValue.fromProto(p.getValue());
        wfRunId = p.getWfRunId();
        name = p.getName();
        threadRunNumber = p.getThreadRunNumber();
        date = LHUtil.fromProtoTs(p.getDate());
    }

    public VariablePb.Builder toProto() {
        VariablePb.Builder out = VariablePb
            .newBuilder()
            .setName(name)
            .setThreadRunNumber(threadRunNumber)
            .setWfRunId(wfRunId)
            .setDate(LHUtil.fromDate(getCreatedAt()))
            .setValue(value.toProto());

        return out;
    }

    public VariableId getObjectId() {
        return new VariableId(wfRunId, threadRunNumber, name);
    }

    public Date getCreatedAt() {
        if (date == null) {
            date = new Date();
        }
        return date;
    }

    public String getPartitionKey() {
        return wfRunId;
    }
}
