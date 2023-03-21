package io.littlehorse.common.model.wfrun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.VariableIdPb;
import io.littlehorse.jlib.common.proto.VariablePb;
import io.littlehorse.jlib.common.proto.VariablePbOrBuilder;
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

    public void initFrom(MessageOrBuilder proto) {
        VariablePbOrBuilder p = (VariablePbOrBuilder) proto;
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

    public String getObjectId() {
        return getObjectId(wfRunId, threadRunNumber, name);
    }

    public static String getObjectId(String wfRunId, int threadNum, String name) {
        return LHUtil.getCompositeId(wfRunId, String.valueOf(threadNum), name);
    }

    public static VariableIdPb parseId(String id) {
        String[] split = id.split("/");
        return VariableIdPb
            .newBuilder()
            .setWfRunId(split[0])
            .setThreadRunNumber(Integer.valueOf(split[1]))
            .setName(split[2])
            .build();
    }

    public static String getObjectId(VariableIdPb id) {
        return getObjectId(id.getWfRunId(), id.getThreadRunNumber(), id.getName());
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
