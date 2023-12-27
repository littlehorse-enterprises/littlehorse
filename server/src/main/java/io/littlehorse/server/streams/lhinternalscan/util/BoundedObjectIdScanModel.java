package io.littlehorse.server.streams.lhinternalscan.util;

import com.google.protobuf.Message;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.proto.BoundedObjectIdScan;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class BoundedObjectIdScanModel extends ScanBoundary<BoundedObjectIdScan> {

    private String startObjectId;
    private String endObjectId;
    private GetableClassEnum scanObjectType;

    public BoundedObjectIdScanModel() {}

    public BoundedObjectIdScanModel(GetableClassEnum scanObjectType, ObjectIdModel<?, ?, ?> startObjectId) {
        this.startObjectId = startObjectId.toString() + "/";
        this.endObjectId = this.startObjectId + "~";
        this.scanObjectType = scanObjectType;
    }

    public BoundedObjectIdScanModel(ObjectIdModel<?, ?, ?> startObjectId, ObjectIdModel<?, ?, ?> endObjectId) {
        this.startObjectId = startObjectId.toString();
        this.endObjectId = endObjectId.toString();
        this.scanObjectType = startObjectId.getType();
    }

    public BoundedObjectIdScanModel(GetableClassEnum scanObjectType, String prefix) {
        this.scanObjectType = scanObjectType;
        this.startObjectId = prefix;
        this.endObjectId = prefix + "~";
    }

    @Override
    public Class<BoundedObjectIdScan> getProtoBaseClass() {
        return BoundedObjectIdScan.class;
    }

    @Override
    public BoundedObjectIdScan.Builder toProto() {
        BoundedObjectIdScan.Builder out =
                BoundedObjectIdScan.newBuilder().setStartObjectId(startObjectId).setScanObjectType(scanObjectType);
        if (endObjectId != null) out.setEndObjectId(endObjectId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        BoundedObjectIdScan p = (BoundedObjectIdScan) proto;
        startObjectId = p.getStartObjectId();
        scanObjectType = p.getScanObjectType();
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

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Storeable<?>> getIterType() {
        return (Class<? extends Storeable<?>>) StoredGetable.class;
    }
}
