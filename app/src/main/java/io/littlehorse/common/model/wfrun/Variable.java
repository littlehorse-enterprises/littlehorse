package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.VariablePb;
import io.littlehorse.common.proto.VariablePbOrBuilder;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

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
            .setDate(LHUtil.fromDate(date))
            .setValue(value.toProto());

        return out;
    }

    @JsonIgnore
    public List<Tag> getTags() {
        List<Tag> out = new ArrayList<>();

        out.add(
            new Tag(
                this,
                Pair.of("type", value.type.toString()),
                Pair.of("name", name)
            )
        );

        if (value.type == VariableTypePb.STR) {
            out.add(
                new Tag(this, Pair.of("strVal", LHUtil.toLhDbFormat(value.strVal)))
            );
        } else if (value.type == VariableTypePb.INT) {
            out.add(
                new Tag(this, Pair.of("intVal", LHUtil.toLhDbFormat(value.intVal)))
            );
        } else if (value.type == VariableTypePb.DOUBLE) {
            out.add(
                new Tag(
                    this,
                    Pair.of("doubleVal", LHUtil.toLhDbFormat(value.doubleVal))
                )
            );
        } else if (value.type == VariableTypePb.BOOL) {
            out.add(
                new Tag(this, Pair.of("boolVal", LHUtil.toLhDbFormat(value.boolVal)))
            );
        } else if (value.type == VariableTypePb.JSON_ARR) {
            // don't do anything yet...in the future we'll do some jsonpath stuff.
        } else if (value.type == VariableTypePb.JSON_OBJ) {
            // don't do anything yet...in the future we'll do some jsonpath stuff.
        }

        return out;
    }

    @JsonIgnore
    public String getSubKey() {
        return getObjectId(wfRunId, threadRunNumber, name);
    }

    @JsonIgnore
    public static String getObjectId(String wfRunId, int threadNum, String name) {
        return wfRunId + "-" + threadNum + "-" + name;
    }

    @JsonIgnore
    public Date getCreatedAt() {
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
