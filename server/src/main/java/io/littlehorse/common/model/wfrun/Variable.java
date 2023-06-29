package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.VariablePb;
import io.littlehorse.server.streamsimpl.storeinternals.GETableIndex;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
public class Variable extends GETable<VariablePb> {

    public VariableValue value;
    public String wfRunId;
    public int threadRunNumber;
    public String name;
    public Date date;

    private WfSpec wfSpec;

    public Class<VariablePb> getProtoBaseClass() {
        return VariablePb.class;
    }

    public WfSpec getWfSpec() {
        return wfSpec;
    }

    public void setWfSpec(WfSpec spec) {
        this.wfSpec = spec;
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

    @Override
    public List<GETableIndex> getIndexes() {
        return List.of(
            new GETableIndex(
                Variable.class,
                List.of(
                    Pair.of("name", geTable -> List.of(((Variable) geTable).name)),
                    Pair.of(
                        "wfSpecName",
                        geTable -> List.of(((Variable) geTable).getWfSpec().getName())
                    ),
                    Pair.of(
                        "wfSpecVersion",
                        geTable ->
                            List.of(
                                LHUtil.toLHDbVersionFormat(
                                    ((Variable) geTable).getWfSpec().version
                                )
                            )
                    )
                ),
                variable -> ((Variable) variable).value.getValueTagPair() != null,
                TagStorageTypePb.LOCAL
            )
        );
    }
}
