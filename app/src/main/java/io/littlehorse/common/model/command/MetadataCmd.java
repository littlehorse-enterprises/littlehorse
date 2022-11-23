package io.littlehorse.common.model.command;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.MetadataCmdPb;
import io.littlehorse.common.proto.MetadataCmdPb.MetadataCmdCase;
import io.littlehorse.common.proto.MetadataCmdPbOrBuilder;

public class MetadataCmd extends LHSerializable<MetadataCmdPb> {

    public MetadataCmdCase type;
    public WfSpec wfSpec;
    public TaskDef taskDef;
    public ExternalEventDef externalEventDef;

    public Class<MetadataCmdPb> getProtoBaseClass() {
        return MetadataCmdPb.class;
    }

    public MetadataCmdPb.Builder toProto() {
        MetadataCmdPb.Builder out = MetadataCmdPb.newBuilder();
        switch (type) {
            case WF_SPEC_CREATED:
                out.setWfSpecCreated(wfSpec.toProto());
                break;
            case TASK_DEF_CREATED:
                out.setTaskDefCreated(taskDef.toProto());
                break;
            case EXTERNAL_EVENT_DEF_CREATED:
                out.setExternalEventDefCreated(externalEventDef.toProto());
                break;
            case METADATACMD_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        MetadataCmdPbOrBuilder p = (MetadataCmdPbOrBuilder) proto;
        type = p.getMetadataCmdCase();
        switch (type) {
            case WF_SPEC_CREATED:
                wfSpec = WfSpec.fromProto(p.getWfSpecCreatedOrBuilder());
                break;
            case TASK_DEF_CREATED:
                taskDef = TaskDef.fromProto(p.getTaskDefCreatedOrBuilder());
                break;
            case EXTERNAL_EVENT_DEF_CREATED:
                externalEventDef =
                    ExternalEventDef.fromProto(
                        p.getExternalEventDefCreatedOrBuilder()
                    );
                break;
            case METADATACMD_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
