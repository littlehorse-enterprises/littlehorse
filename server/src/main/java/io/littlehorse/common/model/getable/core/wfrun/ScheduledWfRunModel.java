package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.ScheduledWfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ScheduledWfRun;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class ScheduledWfRunModel extends CoreGetable<ScheduledWfRun> {
    private ScheduledWfRunIdModel id;
    private WfSpecIdModel wfSpecId;
    private Map<String, VariableValueModel> variables = new HashMap<>();
    private WfRunIdModel parentWfRunId;
    private String cronExpression;
    private Date createdAt;

    public ScheduledWfRunModel() {}

    public ScheduledWfRunModel(
            ScheduledWfRunIdModel id,
            WfSpecIdModel wfSpecId,
            Map<String, VariableValueModel> variables,
            WfRunIdModel parentWfRunId,
            String cronExpression) {
        this.id = id;
        this.wfSpecId = wfSpecId;
        this.variables = variables;
        this.parentWfRunId = parentWfRunId;
        this.cronExpression = cronExpression;
        this.createdAt = new Date();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ScheduledWfRun p = (ScheduledWfRun) proto;
        id = ScheduledWfRunIdModel.fromProto(p.getId(), ScheduledWfRunIdModel.class, context);
        wfSpecId = WfSpecIdModel.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        cronExpression = p.getCronExpression();
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());

        for (Map.Entry<String, VariableValue> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableValueModel.fromProto(e.getValue(), context));
        }

        if (p.hasParentWfRunId()) {
            parentWfRunId = LHSerializable.fromProto(p.getParentWfRunId(), WfRunIdModel.class, context);
        }
    }

    @Override
    public ScheduledWfRun.Builder toProto() {
        ScheduledWfRun.Builder out = ScheduledWfRun.newBuilder()
                .setId(id.toProto())
                .setWfSpecId(wfSpecId.toProto())
                .setCronExpression(cronExpression)
                .setCreatedAt(LHUtil.fromDate(createdAt));
        if (parentWfRunId != null) {
            out.setParentWfRunId(parentWfRunId.toProto());
        }

        for (Map.Entry<String, VariableValueModel> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public Class<ScheduledWfRun> getProtoBaseClass() {
        return ScheduledWfRun.class;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(
                new GetableIndex<>(
                        List.of(Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(Pair.of("majorVersion", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(Pair.of("wfSpecId", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)));
    }

    @Override
    public ScheduledWfRunIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "wfSpecName" -> {
                return List.of(new IndexedField(key, this.wfSpecId.getName(), tagStorageType.get()));
            }
            case "wfSpecId" -> {
                return List.of(new IndexedField(key, wfSpecId.toString(), TagStorageType.LOCAL));
            }
            case "majorVersion" -> {
                return List.of(new IndexedField(
                        key,
                        wfSpecId.getName() + "/" + LHUtil.toLHDbVersionFormat(wfSpecId.getMajorVersion()),
                        TagStorageType.LOCAL));
            }
        }
        return List.of();
    }
}
