package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.metrics.MetricModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.sdk.common.proto.MetricId;
import io.littlehorse.sdk.common.proto.MetricType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;

@Getter
public class MetricIdModel extends MetadataId<MetricId, Metric, MetricModel> {

    private MeasurableObject object;
    private NodeReferenceModel nodeReference;
    private WfSpecIdModel wfSpecId;
    private ThreadSpecReferenceModel threadSpecReference;
    private MetricType metricType;
    private MetricId.ReferenceCase referenceCase;

    public MetricIdModel() {}

    public MetricIdModel(MeasurableObject object, MetricType type) {
        this.object = object;
        this.metricType = type;
        this.referenceCase = MetricId.ReferenceCase.OBJECT;
    }

    public MetricIdModel(NodeReferenceModel nodeReference, MetricType type) {
        this.nodeReference = nodeReference;
        this.metricType = type;
        this.referenceCase = MetricId.ReferenceCase.NODE;
    }

    public MetricIdModel(WfSpecIdModel wfSpecId, MetricType type) {
        this.wfSpecId = wfSpecId;
        this.metricType = type;
        this.referenceCase = MetricId.ReferenceCase.WF_SPEC_ID;
    }

    public MetricIdModel(ThreadSpecReferenceModel threadSpecReference, MetricType type) {
        this.threadSpecReference = threadSpecReference;
        this.metricType = type;
        this.referenceCase = MetricId.ReferenceCase.REFERENCE_NOT_SET;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        MetricId p = (MetricId) proto;
        this.referenceCase = p.getReferenceCase();
        this.object = p.hasObject() ? p.getObject() : null;
        this.nodeReference =
                p.hasNode() ? LHSerializable.fromProto(p.getNode(), NodeReferenceModel.class, context) : null;
        this.threadSpecReference = p.hasThreadSpec()
                ? LHSerializable.fromProto(p.getThreadSpec(), ThreadSpecReferenceModel.class, context)
                : null;
        this.wfSpecId =
                p.hasWfSpecId() ? LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context) : null;
        this.metricType = p.getType();
    }

    @Override
    public MetricId.Builder toProto() {
        MetricId.Builder out = MetricId.newBuilder();
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
        out.setType(metricType);
        return out;
    }

    @Override
    public Class<MetricId> getProtoBaseClass() {
        return MetricId.class;
    }

    @Override
    public String toString() {
        return switch (referenceCase) {
            case OBJECT -> LHUtil.getCompositeId(String.valueOf(referenceCase.getNumber()), String.valueOf(object.getNumber()), metricType.toString());
            case WF_SPEC_ID ->LHUtil.getCompositeId(String.valueOf(referenceCase.getNumber()), wfSpecId.toString(), metricType.toString());
            case THREAD_SPEC -> LHUtil.getCompositeId(String.valueOf(referenceCase.getNumber()), threadSpecReference.toString(), metricType.toString());
            case NODE -> LHUtil.getCompositeId(String.valueOf(referenceCase.getNumber()), nodeReference.toString(), metricType.toString());
            default -> throw new IllegalStateException("Unexpected value: " + referenceCase);
        };
    }

    @Override
    public void initFromString(String storeKey) {
        String[] parts = storeKey.split("/");
        this.referenceCase = MetricId.ReferenceCase.forNumber(Integer.parseInt(parts[0]));
        switch (referenceCase) {
            case OBJECT -> {
                this.object = MeasurableObject.forNumber(Integer.parseInt(parts[1]));
                this.metricType = MetricType.valueOf(parts[2]);
            }
            case WF_SPEC_ID -> {
                this.wfSpecId = new WfSpecIdModel();
                this.wfSpecId.initFromString(storeKey.substring(parts[0].length() + 1, storeKey.indexOf(parts[parts.length - 1]) - 1));
                this.metricType = MetricType.valueOf(parts[parts.length - 1]);
            }
            case null, default -> throw new IllegalStateException("Unexpected value: " + referenceCase);
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
