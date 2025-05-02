package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.metrics.MetricSpecModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.sdk.common.proto.MetricId;
import io.littlehorse.sdk.common.proto.MetricSpec;
import io.littlehorse.sdk.common.proto.MetricSpecId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;

@Getter
public class MetricSpecIdModel extends MetadataId<MetricSpecId, MetricSpec, MetricSpecModel> {

    private MeasurableObject object;
    private NodeReferenceModel nodeReference;
    private WfSpecIdModel wfSpecId;
    private ThreadSpecReferenceModel threadSpecReference;
    private MetricSpecId.ReferenceCase referenceCase;

    public MetricSpecIdModel() {}

    public MetricSpecIdModel(MeasurableObject object) {
        this.object = object;
        this.referenceCase = MetricSpecId.ReferenceCase.OBJECT;
    }

    public MetricSpecIdModel(NodeReferenceModel nodeReference) {
        this.nodeReference = nodeReference;
        this.referenceCase = MetricSpecId.ReferenceCase.NODE;
    }

    public MetricSpecIdModel(WfSpecIdModel wfSpecId) {
        this.wfSpecId = wfSpecId;
        this.referenceCase = MetricSpecId.ReferenceCase.WF_SPEC_ID;
    }

    public MetricSpecIdModel(ThreadSpecReferenceModel threadSpecReference) {
        this.threadSpecReference = threadSpecReference;
        this.referenceCase = MetricSpecId.ReferenceCase.THREAD_SPEC;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MetricSpecId p = (MetricSpecId) proto;
        this.referenceCase = p.getReferenceCase();
        this.object = p.hasObject() ? p.getObject() : null;
        this.nodeReference =
                p.hasNode() ? LHSerializable.fromProto(p.getNode(), NodeReferenceModel.class, context) : null;
        this.threadSpecReference = p.hasThreadSpec()
                ? LHSerializable.fromProto(p.getThreadSpec(), ThreadSpecReferenceModel.class, context)
                : null;
        this.wfSpecId =
                p.hasWfSpecId() ? LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context) : null;
    }

    @Override
    public MetricSpecId.Builder toProto() {
        MetricSpecId.Builder out = MetricSpecId.newBuilder();
        if (object != null) {
            out.setObject(object);
        }
        if (nodeReference != null) {
            out.setNode(nodeReference.toProto());
        }
        if (threadSpecReference != null) {
            out.setThreadSpec(threadSpecReference.toProto());
        }
        if (wfSpecId != null) {
            out.setWfSpecId(wfSpecId.toProto());
        }
        return out;
    }

    @Override
    public Class<MetricId> getProtoBaseClass() {
        return MetricId.class;
    }

    @Override
    public String toString() {
        return switch (referenceCase) {
            case OBJECT -> LHUtil.getCompositeId(
                    String.valueOf(referenceCase.getNumber()), String.valueOf(object.getNumber()));
            case WF_SPEC_ID -> LHUtil.getCompositeId(String.valueOf(referenceCase.getNumber()), wfSpecId.toString());
            case THREAD_SPEC -> LHUtil.getCompositeId(
                    String.valueOf(referenceCase.getNumber()), threadSpecReference.toString());
            case NODE -> LHUtil.getCompositeId(String.valueOf(referenceCase.getNumber()), nodeReference.toString());
            default -> throw new IllegalStateException("Unexpected value: " + referenceCase);
        };
    }

    @Override
    public void initFromString(String storeKey) {
        String[] parts = storeKey.split("/");
        this.referenceCase = MetricSpecId.ReferenceCase.forNumber(Integer.parseInt(parts[0]));
        switch (referenceCase) {
            case OBJECT -> {
                this.object = MeasurableObject.forNumber(Integer.parseInt(parts[1]));
            }
            case WF_SPEC_ID -> {
                this.wfSpecId = new WfSpecIdModel();
                this.wfSpecId.initFromString(storeKey.substring(parts[0].length() + 1));
            }
            default -> throw new IllegalStateException("Unexpected value: " + referenceCase);
        }
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.METRIC;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(this.toString());
    }
}
