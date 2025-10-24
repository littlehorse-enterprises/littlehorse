package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.WfRunStoredInventory;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class WfRunStoredInventoryModel extends Storeable<WfRunStoredInventory> {

    @Getter
    @Setter
    private WfRunIdModel wfRunId;

    @Getter
    private List<ExternalEventIdModel> externalEventIds;

    public WfRunStoredInventoryModel() {
        externalEventIds = new ArrayList<>();
    }

    @Override
    public Class<WfRunStoredInventory> getProtoBaseClass() {
        return WfRunStoredInventory.class;
    }

    @Override
    public WfRunStoredInventory.Builder toProto() {
        WfRunStoredInventory.Builder out = WfRunStoredInventory.newBuilder().setWfRunId(wfRunId.toProto());
        for (ExternalEventIdModel externalEventId : externalEventIds) {
            out.addExternalEvents(externalEventId.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        WfRunStoredInventory p = (WfRunStoredInventory) proto;

        wfRunId = WfRunIdModel.fromProto(p.getWfRunId(), WfRunIdModel.class, ignored);

        for (ExternalEventId externalEventId : p.getExternalEventsList()) {
            externalEventIds.add(ExternalEventIdModel.fromProto(externalEventId, ExternalEventIdModel.class, ignored));
        }
    }

    @Override
    public String getStoreKey() {
        return wfRunId.getStoreableKey(StoreableType.WFRUN_STORED_INVENTORY);
    }

    @Override
    public StoreableType getType() {
        return StoreableType.WFRUN_STORED_INVENTORY;
    }
}
