package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.VariablePb;
import io.littlehorse.common.proto.VariablePbOrBuilder;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class Variable extends GETable<VariablePb> {

    @JsonIgnore
    public VariableValue value;

    @JsonIgnore
    public String wfRunId;

    @JsonIgnore
    public int threadRunNumber;

    @JsonIgnore
    public String name;

    @JsonIgnore
    public Date date;

    @JsonIgnore
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

    @JsonIgnore
    public String getObjectId() {
        return getStoreKey(wfRunId, threadRunNumber, name);
    }

    @JsonIgnore
    public static String getStoreKey(String wfRunId, int threadNum, String name) {
        return wfRunId + "-" + threadNum + "-" + name;
    }

    @JsonIgnore
    public Date getCreatedAt() {
        if (date == null) {
            date = new Date();
        }
        return date;
    }

    @JsonIgnore
    public String getPartitionKey() {
        return wfRunId;
    }

    // The below is just for Jackson
    public VariableTypePb getType() {
        return value.type;
    }

    public Object getVal() {
        return value.getVal();
    }
    // End Jackson section
}
