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
import io.littlehorse.sdk.common.exception.LHSerdeError;
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

public class ScheduledWfRunModel extends CoreGetable<ScheduledWfRun> {
    private ScheduledWfRunIdModel id;
    private WfSpecIdModel wfSPecId;
    private Map<String, VariableValueModel> variables = new HashMap<>();
    private WfRunIdModel parentWfRunId;
    private String cronExpression;
    private Date createdAt;

    public ScheduledWfRunModel() {}

    public ScheduledWfRunModel(
            ScheduledWfRunIdModel id,
            WfSpecIdModel wfSPecId,
            Map<String, VariableValueModel> variables,
            WfRunIdModel parentWfRunId,
            String cronExpression) {
        this.id = id;
        this.wfSPecId = wfSPecId;
        this.variables = variables;
        this.parentWfRunId = parentWfRunId;
        this.cronExpression = cronExpression;
        this.createdAt = new Date();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ScheduledWfRun p = (ScheduledWfRun) proto;
        id = ScheduledWfRunIdModel.fromProto(p.getId(), ScheduledWfRunIdModel.class, context);
        wfSPecId = WfSpecIdModel.fromProto(proto, WfSpecIdModel.class, context);
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
                .setWfSpecId(wfSPecId.toProto())
                .setCronExpression(cronExpression)
                .setCreatedAt(LHUtil.fromDate(createdAt));
        if (parentWfRunId != null) {
            out.setParentWfRunId(parentWfRunId.toProto());
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
        return List.of();
    }

    @Override
    public ScheduledWfRunIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return null;
    }
}
