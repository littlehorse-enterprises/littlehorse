package io.littlehorse.server.model.internal;

import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.MetadataEventPb;
import io.littlehorse.common.proto.PutTaskDefPb;
import io.littlehorse.common.proto.MetadataEventPb.EventCase;

public class MetadataEvent extends LHSerializable<MetadataEventPb> {
    public EventCase type;

    public PutWfSpec putWfSpec;
    public PutTaskDef putTaskDef;
    public DeleteTaskDef deleteTaskDef;
    public DeleteWfSpec deleteWfSpec;

    public Class<MetadataEventPb> getProtoBaseClass() {
        return MetadataEventPb.class;
    }

    public MetadataEventPb.Builder toProto() {
        MetadataEventPb.Builder out = MetadataEventPb.newBuilder();
        switch (type) {
        case PUT_WF_SPEC:
            out.setPutWfSpec(putWfSpec.toProto());
            break;
        case PUT_TASK_DEF:
            out.setPutTaskDef(putTaskDef.toProto());
            break;
        case DELETE_TASK_DEF:
            out.setDeleteTaskDef(deleteTaskDef.toProto());
            break;
        case DELETE_WF_SPEC:
            out.setDeleteWfSpec(deleteWfSpec.toProto());
            break;
        case EVENT_NOT_SET:
            break;
        }
        return out;
    }

    public void initFrom(MetadataEventPb proto) {
        type = proto.getEventCase();
        switch (type) {
            case PUT_WF_SPEC:
                putWfSpec = PutWfSpec.fromProto(proto.getPutWfSpec());
                break;
            case PUT_TASK_DEF:
                putTaskDef = PutTaskDef.fromProto(proto.getPutTaskDef());
                break;
            case DELETE_TASK_DEF:
                deleteTaskDef = DeleteTaskDef.fromProto(proto.getDeleteTaskDef());
                break;
            case DELETE_WF_SPEC:
                deleteWfSpec = DeleteWfSpec.fromProto(proto.getDeleteWfSpec());
                break;
            case EVENT_NOT_SET:
                break;
            }
    }
}
