package io.littlehorse.server.streams.lhinternalscan.util;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.proto.BoundedObjectIdScan;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class BoundedObjectIdScanModel extends ScanBoundary<BoundedObjectIdScan> {

    private String startObjectId;
    private String endObjectId;

    public BoundedObjectIdScanModel() {}

    public BoundedObjectIdScanModel(ObjectIdModel<?, ?, ?> startObjectId) {
        this.startObjectId = startObjectId.toString();
    }

    public BoundedObjectIdScanModel(ObjectIdModel<?, ?, ?> startObjectId, ObjectIdModel<?, ?, ?> endObjectId) {
        this.startObjectId = startObjectId.toString();
        this.endObjectId = endObjectId.toString();
    }

    @Override
    public Class<BoundedObjectIdScan> getProtoBaseClass() {
        return BoundedObjectIdScan.class;
    }

    @Override
    public BoundedObjectIdScan.Builder toProto() {
        BoundedObjectIdScan.Builder out = BoundedObjectIdScan.newBuilder().setStartObjectId(startObjectId);
        if (endObjectId != null) out.setEndObjectId(endObjectId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        BoundedObjectIdScan p = (BoundedObjectIdScan) proto;
        startObjectId = p.getStartObjectId();
        if (p.hasEndObjectId()) endObjectId = p.getEndObjectId();
    }

    @Override
    public String getStartKey() {
        return startObjectId;
    }

    @Override
    public String getEndKey() {
        return endObjectId == null ? startObjectId + "~" : endObjectId;
    }
}
