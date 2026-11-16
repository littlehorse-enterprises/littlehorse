package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.WorkflowRetentionPolicyModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.InlineWfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.InlineWfSpec;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InlineWfSpecModel extends CoreGetable<InlineWfSpec> {

    private WfRunIdModel id;
    private Date createdAt;
    private Map<String, ThreadSpecModel> threadSpecs = new HashMap<>();
    private String entrypointThreadName;
    private WorkflowRetentionPolicyModel retentionPolicy;

    // Runtime view only. Inline definitions have no registered metadata ID.
    public WfSpecModel asWfSpecModel() {
        WfSpecModel spec = new WfSpecModel();
        spec.setId(null);
        spec.setThreadSpecs(threadSpecs);
        spec.setEntrypointThreadName(entrypointThreadName);
        spec.setRetentionPolicy(retentionPolicy);
        spec.getThreadSpecs().forEach((name, thread) -> {
            thread.setName(name);
            thread.setWfSpec(spec);
        });
        return spec;
    }

    @Override
    public Class<InlineWfSpec> getProtoBaseClass() {
        return InlineWfSpec.class;
    }

    @Override
    public InlineWfSpec.Builder toProto() {
        InlineWfSpec.Builder out = InlineWfSpec.newBuilder();
        if (id != null) out.setId(id.toProto());
        if (createdAt != null) out.setCreatedAt(LHUtil.fromDate(createdAt));
        out.setEntrypointThreadName(entrypointThreadName);
        threadSpecs.forEach(
                (name, thread) -> out.putThreadSpecs(name, thread.toProto().build()));
        if (retentionPolicy != null) out.setRetentionPolicy(retentionPolicy.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        InlineWfSpec p = (InlineWfSpec) proto;
        id = p.hasId() ? LHSerializable.fromProto(p.getId(), WfRunIdModel.class, context) : null;
        createdAt = p.hasCreatedAt() ? LHUtil.fromProtoTs(p.getCreatedAt()) : null;
        entrypointThreadName = p.getEntrypointThreadName();
        threadSpecs.clear();
        p.getThreadSpecsMap().forEach((name, thread) -> {
            ThreadSpecModel model = LHSerializable.fromProto(thread, ThreadSpecModel.class, context);
            model.setName(name);
            threadSpecs.put(name, model);
        });
        retentionPolicy = p.hasRetentionPolicy()
                ? LHSerializable.fromProto(p.getRetentionPolicy(), WorkflowRetentionPolicyModel.class, context)
                : null;
    }

    @Override
    public InlineWfSpecIdModel getObjectId() {
        return new InlineWfSpecIdModel(id);
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }
}
